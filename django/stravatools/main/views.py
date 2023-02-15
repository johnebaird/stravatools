import logging

from django.http import HttpResponse, HttpResponseRedirect
from django.shortcuts import render, redirect
from django.contrib.auth.forms import UserCreationForm

from . import stravaapi

logger = logging.getLogger(__name__)

# Create your views here.

def index(request):
   
    return render(request, 'main/index.html')
    
def register(request):
    # redirect to index page if they haven't authed to strava yet
    if 'bearer' not in request.session: 
        return redirect(index)

    if request.method == 'POST':

        form = UserCreationForm(request.POST)
        if form.is_valid():
            user = form.save()
    
    else:
        form = UserCreationForm()
    
    return render(request, 'registration/register.html', {'form': form})
    

def exchange_token(request):
    request.session['bearer'] = stravaapi.getBearerFromCode(request.GET['code'])
    return redirect(register)

def activities(request):
    if 'bearer' not in request.session:
        return redirect(index)
    
    logger.debug("using bearer token: " + request.session['bearer'])
    athlete = stravaapi.getAthlete(request.session['bearer'])
    return render(request, "activities.html", {'athlete', athlete})