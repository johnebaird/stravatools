import logging

from django.shortcuts import render, redirect
from .forms import DefaultBikesForm
from .models import DefaultBike, Bike
from main.views import checkbearer, index, get_bike_choices

logger = logging.getLogger(__name__)

# Create your views here.

def defaultbikes(request):

    if not checkbearer(request): return redirect(index)

    if 'bikechoices' not in request.session:
        request.session['bikechoices'] = get_bike_choices(request)

    if request.method == 'POST':

        if request.user.profile.defaultbikes:
            form = DefaultBikesForm(request.user.profile, request.POST, instance=request.user.profile.defaultbikes)
        else:
            form = DefaultBikesForm(request.user.profile, request.POST)

        if form.is_valid():
            logger.debug("Form is valid")

            request.user.profile.defaultbikes = form.save()
            request.user.profile.save()
            
            
    else:
        if request.user.is_authenticated:
            updatebikes(request)
            if request.user.profile.defaultbikes:
                form = DefaultBikesForm(request.user.profile, instance=request.user.profile.defaultbikes)
            else:
                form = DefaultBikesForm(request.user.profile)
        else:
            form = DefaultBikesForm()
            for fields in form.fields.values():
                fields.disabled = True
    
    return render(request, 'defaultbikes/defaultbikes.html', {'form': form})


def updatebikes(request) -> None:
    if 'athlete' not in request.session or 'bikes' not in request.session['athlete']:
        return
    
    for bike in request.session['athlete']['bikes']:
        Bike.objects.update_or_create(id=bike['id'], defaults={'name':bike['name'], 'profile':request.user.profile})
