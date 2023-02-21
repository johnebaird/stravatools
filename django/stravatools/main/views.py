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

            return redirect(activities)
    
    else:
        if request.user.is_authenticated:
            stravaapi.refreshBearer(request.user.profile.bearer)
            bearer = request.user.profile.bearer.access_token
        else:
            if 'bearer' in request.session:
                bearer = request.session['bearer']['access_token']
            else:
                return redirect(index)
    
        activity = stravaapi.getActivityFromId(bearer, id)
        athlete = stravaapi.getAthlete(bearer)

        initial = {'commute': activity['commute'],
                   'trainer': activity['trainer'],
                   'hide_from_home': activity['hide_from_home'], 
                   'description': activity['description'],
                   'name': activity['name'],
                   'sport_type': activity['sport_type'], 
                   'gear_id': activity['gear_id']}

        form = UpdatableActivity(initial=initial)
        
        # make tuples of bike and id for choice object drop down
        bikechoices = []
        for b in athlete['bikes']:
            bikechoices.append((b['id'], b['nickname']))        
        form.populate_bikes(bikechoices)

        request.session['activity'] = activity
        request.session['bikes'] = bikechoices
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

    page = request.GET.get('page', '1')

    if request.user.is_authenticated:
        stravaapi.refreshBearer(request.user.profile.bearer)
        bearer = request.user.profile.bearer.access_token
    else:
        if 'bearer' in request.session:
            bearer = request.session['bearer']['access_token']
        else:
            return redirect(index)
        
    activities = stravaapi.getActivities(bearer, page)
    athlete = stravaapi.getAthlete(bearer)

    # add gear name along with id to activities dict
    for bike in athlete['bikes']:
        for a in activities:
            if a['gear_id'] == bike['id']:
                a['gear_name'] = bike['nickname']


    return render(request, "main/activities.html", context={"activities": activities})