import logging

from django.shortcuts import render, redirect
from main.utils import checkbearer, get_bike_choices
from main import stravaapi

from .forms import UpdatableActivity

logger = logging.getLogger(__name__)

# Create your views here.

def activitydetail(request, id):

    if not checkbearer(request): return redirect("main:index")
    if not request.session['stravawrite']: return redirect('activities:activities')
    
    if request.method == 'POST':

        form = UpdatableActivity(request.POST, initial=request.session['initial'])
        
        # bit hacky but since gear_id choices were set programmatically not statically 
        # django doesn't have them from request.POST but we to add them again to validate
        form.fields['gear_id'].choices = request.session['bikechoices']
        
        if form.is_valid():
            logger.debug("form submit is good")

            update = {}
            for data in form.changed_data:
                update[data] = form.cleaned_data[data]

            logger.debug("Updating activity with:" + str(update))
            stravaapi.updateActivity(request.session['access_token'], id, update)
            
            # we'll need to reload activitydata to see changes
            if 'activitydata' in request.session:
                del request.session['activitydata']

            return redirect('activities:activities')
    
    else:
        activity = stravaapi.getActivityFromId(request.session['access_token'], id)
        
        initial = {'commute': activity['commute'],
                   'trainer': activity['trainer'],
                   'hide_from_home': activity['hide_from_home'], 
                   'description': activity['description'],
                   'name': activity['name'],
                   'gear_id': activity['gear_id']}

        form = UpdatableActivity(initial=initial)
        form.distance = activity['distance']
        form.sport_type = activity['sport_type']

        if request.user.is_authenticated and request.user.profile.distance == 'miles':
            activity['distance'] = round(activity['distance'] * 0.000621371,2)
        else:
            activity['distance'] = round(activity['distance'] * 0.001,2)
        
        # make tuples of bike and id for choice object drop down
        if 'bikechoices' not in request.session:
            request.session['bikechoices'] = get_bike_choices(request)

        form.fields['gear_id'].choices = request.session['bikechoices']

        request.session['activity'] = activity
        request.session['initial'] = initial

    return render(request, 'activities/activitydetail.html', {'form': form, 'id': id})


def activities(request):

    if not checkbearer(request): return redirect("main:index")

    page = request.GET.get('page', '1')

    # save activity data so we don't query API on every request
    if 'activitydata' in request.session:
        activities = request.session['activitydata']
    else:
        activities = stravaapi.getActivities(request.session['access_token'], page)
        # add gear name along with id to activities dict
        for bike in request.session['athlete']['bikes']:
            for a in activities:
                if a['gear_id'] == bike['id']:
                    a['gear_name'] = bike['nickname']
        
        if request.user.is_authenticated and request.user.profile.distance == 'miles':
            for a in activities:
                a['distance'] = round(a['distance'] * 0.000621371,2)
        else:
            for a in activities:
                a['distance'] = round(a['distance'] * 0.001,2)

        request.session['activitydata'] = activities

    return render(request, "activities/activities.html", context={"activities": activities, 'stravawrite': request.session['stravawrite']})
