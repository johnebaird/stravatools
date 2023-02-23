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
        
        form = DefaultBikesForm(request.POST)
        form.fields['indoor_bike'].choices = request.session['bikechoices']
        form.fields['outdoor_bike'].choices = request.session['bikechoices']

        if form.is_valid():
            logger.debug("Form is valid")
            
            request.user.profile.defaultbikes = form.save()
            request.user.profile.defaultbikes.profile = request.user.profile
            
            request.user.profile.defaultbikes.indoor_bike, created = Bike.objects.get_or_create(id=form.cleaned_data['indoor_bike'])
            request.user.profile.defaultbikes.outdoor_bike, created = Bike.objects.get_or_create(id=form.cleaned_data['outdoor_bike'])

            del created

            for bike in request.session['athlete']['bikes']:
                if bike['id'] == form.cleaned_data['indoor_bike']:
                    request.user.profile.defaultbikes.indoor_bike.name = bike['name']

                if bike['id'] == form.cleaned_data['outdoor_bike']:
                    request.user.profile.defaultbikes.outdoor_bike.name = bike['name']

            request.user.profile.defaultbikes.indoor_bike.save()
            request.user.profile.defaultbikes.outdoor_bike.save()

            request.user.profile.defaultbikes.save()
            request.user.profile.save()

    else:
        form = DefaultBikesForm(instance=request.user.profile.defaultbikes)

        form.fields['indoor_bike'].choices = request.session['bikechoices']
        form.fields['outdoor_bike'].choices = request.session['bikechoices']

    return render(request, 'defaultbikes/defaultbikes.html', {'form': form})
