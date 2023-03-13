import logging

from django.shortcuts import render, redirect
from django.forms import formset_factory

from .forms import DefaultBikesForm, ManualBikeCorrection
from .models import DefaultBike, Bike
from main.utils import checkbearer, get_bike_choices
from main.views import index, activities
from main.models import Logging
from main import stravaapi

logger = logging.getLogger(__name__)

# Create your views here.

def defaultbikes(request):

    if not checkbearer(request): return redirect(index)
    if not request.session['stravawrite']: return redirect(activities)
    
    results = []
    changelog = {}
    
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

                bikedata = {}
                for bike in request.session['athlete']['bikes']:
                    if manualbikecorrectionform.cleaned_data['bike'] == bike['id']:
                        bikedata = bike

                results = stravaapi.changeActivityDateRange(request.session['access_token'], 
                                                  after=manualbikecorrectionform.cleaned_data['after'],
                                                  before=manualbikecorrectionform.cleaned_data['before'], 
                                                  bike=bikedata, 
                                                  change=manualbikecorrectionform.cleaned_data['activitytype'])
            # we'll need to reload activitydata to see changes
            if 'activitydata' in request.session:
                del request.session['activitydata']

                
    try:
        defaultbikesform
    except NameError:
        if request.user.is_authenticated:
            updatebikes(request)
            if request.user.profile.defaultbikes:
                defaultbikesform = DefaultBikesForm(request.user.profile, instance=request.user.profile.defaultbikes)
                changelog = Logging.objects.filter(profile=request.user.profile, application='autobikechange')\
                                            .order_by('datetime')[:10]
            else:
                defaultbikesform = DefaultBikesForm(request.user.profile)
        else:
            defaultbikesform = DefaultBikesForm(None)

    try:
        manualbikecorrectionform
    except NameError:
        manualbikecorrectionform = ManualBikeCorrection()
        manualbikecorrectionform.fields['bike'].choices = get_bike_choices(request)
    
    return render(request, 'defaultbikes/defaultbikes.html', {'defaultbikesform': defaultbikesform, 
                                                              'manualbikecorrectionform': manualbikecorrectionform, 
                                                              'results': results, 
                                                              'changelog': changelog})


def updatebikes(request) -> None:
    if 'athlete' not in request.session or 'bikes' not in request.session['athlete']:
        return
    
    for bike in request.session['athlete']['bikes']:
        Bike.objects.update_or_create(id=bike['id'], defaults={'name' : bike['name'], 
                                                               'distance' : bike['distance'],
                                                               'profile':request.user.profile})
        