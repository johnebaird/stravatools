import logging

from django.shortcuts import render, redirect
from django.forms import formset_factory

from .forms import DefaultBikesForm, ManualBikeCorrection
from .models import DefaultBike, Bike
from main.views import checkbearer, index, get_bike_choices
from main import stravaapi

logger = logging.getLogger(__name__)

# Create your views here.

def defaultbikes(request):

    if not checkbearer(request): return redirect(index)

    if request.method == 'POST':
        if 'defaultbikes' in request.POST:
            if request.user.profile.defaultbikes:
                defaultbikesform = DefaultBikesForm(request.user.profile, request.POST, instance=request.user.profile.defaultbikes)
            else:
                defaultbikesform = DefaultBikesForm(request.user.profile, request.POST)

            if defaultbikesform.is_valid():
                logger.debug("Default Bikes Form is valid")

                request.user.profile.defaultbikes = defaultbikesform.save()
                request.user.profile.save()

        if 'manualbikecorrection' in request.POST:
            manualbikecorrectionform = ManualBikeCorrection(request.POST)
            manualbikecorrectionform.fields['bike'].choices = get_bike_choices(request)

            if manualbikecorrectionform.is_valid():
                logger.debug("Manual Bike Correction form is valid")

                stravaapi.changeActivityDateRange(request.session['access_token'], 
                                                  after=manualbikecorrectionform.cleaned_data['after'],
                                                  before=manualbikecorrectionform.cleaned_data['before'], 
                                                  bike=manualbikecorrectionform.cleaned_data['bike'], 
                                                  change=manualbikecorrectionform.cleaned_data['activitytype'])
                
    try:
        defaultbikesform
    except NameError:
        if request.user.is_authenticated:
            updatebikes(request)
            if request.user.profile.defaultbikes:
                defaultbikesform = DefaultBikesForm(request.user.profile, instance=request.user.profile.defaultbikes)
            else:
                defaultbikesform = DefaultBikesForm(request.user.profile)
        else:
            defaultbikesform = DefaultBikesForm(None)

    try:
        manualbikecorrectionform
    except NameError:
        manualbikecorrectionform = ManualBikeCorrection()
        manualbikecorrectionform.fields['bike'].choices = get_bike_choices(request)
    
    return render(request, 'defaultbikes/defaultbikes.html', {'defaultbikesform': defaultbikesform, 'manualbikecorrectionform': manualbikecorrectionform})


def updatebikes(request) -> None:
    if 'athlete' not in request.session or 'bikes' not in request.session['athlete']:
        return
    
    for bike in request.session['athlete']['bikes']:
        Bike.objects.update_or_create(id=bike['id'], defaults={'name':bike['name'], 'profile':request.user.profile})
