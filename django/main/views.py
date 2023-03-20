import logging

from django.shortcuts import render, redirect
from django.contrib.auth.forms import UserCreationForm, PasswordChangeForm
from django.contrib.auth import login
from django.contrib.auth.decorators import login_required

from . import stravaapi
from .models import Bearer, Logging

logger = logging.getLogger(__name__)

# Create your views here.

def index(request):
    if request.user.is_authenticated and request.user.profile.bearer:
        return redirect('activities:activities')
    else:
        return render(request, 'main/index.html')

@login_required
def profile(request):

    changelog = Logging.objects.filter(profile=request.user.profile).order_by('datetime')[:10]
    
    if request.method == 'POST':
        passwordform = PasswordChangeForm(request.user,request.POST)
        if passwordform.is_valid():
            passwordform.save()
            login(request, request.user)

    else:
        passwordform = PasswordChangeForm(request.user)

    return render(request, 'registration/profile.html', {'passwordform': passwordform, 'changelog': changelog})
    
def register(request):
    # redirect to index page if they haven't authed to strava yet
    if 'bearer' not in request.session: 
        return redirect('main:index')

    if request.method == 'POST':

        form = UserCreationForm(request.POST)
        if form.is_valid():
            user = form.save()
            login(request, user)

            user.profile.bearer = Bearer()
            user.profile.bearer.load_json(request.session['bearer'])
            user.profile.bearer.write_access = request.session['stravawrite']
            user.profile.bearer.save()
            user.profile.save()
            
            del request.session['bearer']
            
            return redirect('activities:activities')
    else:
        form = UserCreationForm()
    
    return render(request, 'registration/register.html', {'form': form})
    

def exchange_token(request):
    
    # need at least read access
    if ('scope' not in request.GET):
        return redirect('main:index')
    if 'activity:read_all' not in request.GET['scope'] or 'profile:read_all' not in request.GET['scope']:
        return redirect('main:index')

    request.session['bearer'] = stravaapi.getBearerFromCode(request.GET['code'])
    
    # we don't have write access to activities
    request.session['stravawrite'] = 'activity:write' in request.GET['scope']

    request.session.modified = True

    if (request.user.is_authenticated):
            
            # overwrite current bearer token if user is authenticated
            request.user.profile.bearer.load_json(request.session['bearer'])
            request.user.profile.bearer.write_access = request.session['stravawrite']
            request.user.profile.bearer.save()
            request.user.profile.save()
            
            del request.session['bearer']
            return redirect('main:profile')
    
    return redirect('main:register')




