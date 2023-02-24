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
            defaultbikesform = DefaultBikesForm(request.POST, 
                                                instance=request.user.profile.defaultbikes)
        else:
            defaultbikesform = DefaultBikesForm(request.POST)

        defaultbikesform.fields['indoor_bike'].choices = request.session['bikechoices']
        defaultbikesform.fields['outdoor_bike'].choices = request.session['bikechoices']

        if defaultbikesform.is_valid():
            logger.debug("Form is valid")
            
            request.user.profile.defaultbikes = defaultbikesform.save()
            request.user.profile.defaultbikes.profile = request.user.profile
            
            request.user.profile.defaultbikes.indoor_bike, created = Bike.objects.get_or_create(id=defaultbikesform.cleaned_data['indoor_bike'])
            request.user.profile.defaultbikes.outdoor_bike, created = Bike.objects.get_or_create(id=defaultbikesform.cleaned_data['outdoor_bike'])

            del created

            for bike in request.session['athlete']['bikes']:
                if bike['id'] == defaultbikesform.cleaned_data['indoor_bike']:
                    request.user.profile.defaultbikes.indoor_bike.name = bike['name']

                if bike['id'] == defaultbikesform.cleaned_data['outdoor_bike']:
                    request.user.profile.defaultbikes.outdoor_bike.name = bike['name']

            request.user.profile.defaultbikes.indoor_bike.save()
            request.user.profile.defaultbikes.outdoor_bike.save()

            request.user.profile.defaultbikes.save()
            request.user.profile.save()

    else:
        if request.user.is_authenticated:
            if request.user.profile.defaultbikes:
                defaultbikesform = DefaultBikesForm(instance=request.user.profile.defaultbikes,
                                    initial={'indoor_bike': request.user.profile.defaultbikes.indoor_bike.id,
                                             'outdoor_bike': request.user.profile.defaultbikes.outdoor_bike.id})
            else:
                defaultbikesform = DefaultBikesForm()
        else:
            defaultbikesform = DefaultBikesForm()
            for fields in defaultbikesform.fields.values():
                fields.disabled = True
    
        defaultbikesform.fields['indoor_bike'].choices = request.session['bikechoices']
        defaultbikesform.fields['outdoor_bike'].choices = request.session['bikechoices']

    return render(request, 'defaultbikes/defaultbikes.html', {'defaultbikesform': defaultbikesform})
