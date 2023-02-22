import logging

from django.http import HttpResponse, HttpResponseRedirect
from django.shortcuts import render, redirect
from django.contrib.auth.forms import UserCreationForm, AuthenticationForm
from django.template import Context
from django.contrib.auth import authenticate, login

from . import stravaapi
from .models import Bearer, Profile
from .forms import UpdatableActivity

logger = logging.getLogger(__name__)

# Create your views here.

def index(request):
    if request.user.is_authenticated:
        return redirect(activities)
    else:
        return render(request, 'main/index.html')

def profile(request):
    return redirect(activities)
    
def activitydetail(request, id):

    if not checkbearer(request): return redirect(index)
    if not 'athlete' in request.session: 
        request.session['athlete'] = stravaapi.getAthlete(request.session['access_token'])

    if request.method == 'POST':

        form = UpdatableActivity(request.POST, initial=request.session['initial'])
        
        # bit hacky but since gear_id choices were set programmatically not statically 
        # django doesn't have them from request.POST but we do need them to validate
        form.fields['gear_id'].choices = request.session['bikes']
        
        if form.is_valid():
            logger.debug("form submit is good")

            update = {}
            for data in form.changed_data:
                update[data] = form.cleaned_data[data]

            logger.debug("Updating activity with:" + str(update))
            stravaapi.updateActivity(request.session['access_token'], id, update)

            return redirect(activities)
    
    else:
        activity = stravaapi.getActivityFromId(request.session['access_token'], id)
        
        initial = {'commute': activity['commute'],
                   'trainer': activity['trainer'],
                   'hide_from_home': activity['hide_from_home'], 
                   'description': activity['description'],
                   'name': activity['name'],
                   'gear_id': activity['gear_id']}

        form = UpdatableActivity(initial=initial)
        
        # make tuples of bike and id for choice object drop down
        bikechoices = []
        for b in request.session['athlete']['bikes']:
            bikechoices.append((b['id'], b['nickname']))        
        form.populate_bikes(bikechoices)
        
        request.session['bikes'] = bikechoices
        request.session['activity'] = activity
        request.session['initial'] = initial

    return render(request, 'main/activitydetail.html', {'form': form, 'id': id, 'activity': request.session['activity']})

def register(request):
    # redirect to index page if they haven't authed to strava yet
    if 'bearer' not in request.session: 
        return redirect(index)

    if request.method == 'POST':

        form = UserCreationForm(request.POST)
        if form.is_valid():
            user = form.save()
            login(request, user)

            user.profile.bearer = Bearer()
            user.profile.bearer.load_json(request.session['bearer'])
            user.profile.bearer.save()
            user.profile.save()
            
            del request.session['bearer']
            
            return redirect(activities)
    else:
        form = UserCreationForm()
    
    return render(request, 'registration/register.html', {'form': form})
    

def exchange_token(request):
    request.session['bearer'] = stravaapi.getBearerFromCode(request.GET['code'])
    request.session.modified = True
    return redirect(register)


def activities(request):

    if not checkbearer(request): return redirect(index)
    if not 'athlete' in request.session: 
        request.session['athlete'] = stravaapi.getAthlete(request.session['access_token'])

    page = request.GET.get('page', '1')

    activities = stravaapi.getActivities(request.session['access_token'], page)

    # add gear name along with id to activities dict
    for bike in request.session['athlete']['bikes']:
        for a in activities:
            if a['gear_id'] == bike['id']:
                a['gear_name'] = bike['nickname']

    return render(request, "main/activities.html", context={"activities": activities})

# check bearer token, populate from user data if user is logged in and set in session
# otherwise make sure bearer is in session data for users without accounts and if not redirect
def checkbearer(request) -> bool:
    if request.user.is_authenticated:
        stravaapi.refreshBearer(request.user.profile.bearer)
        request.session['access_token'] = request.user.profile.bearer.access_token
        request.session.modified = True
        return True
    else:
        if 'bearer' in request.session:
            request.session['access_token'] = request.session['bearer']['access_token']
            request.session.modified = True
            return True
    return False
