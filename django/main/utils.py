from . import stravaapi
from django.http import request


# check bearer token, populate from user data if user is logged in and set in session
# otherwise make sure bearer is in session data for users without accounts and if not redirect
def checkbearer(request) -> bool:
    if request.user.is_authenticated and request.user.profile.bearer:
        stravaapi.refreshBearer(request.user.profile.bearer)
        request.session['access_token'] = request.user.profile.bearer.access_token
        if not 'athlete' in request.session: request.session['athlete'] = stravaapi.getAthlete(request.session['access_token'])
        request.session.modified = True
        return True
    else:
        if 'bearer' in request.session:
            request.session['access_token'] = request.session['bearer']['access_token']
            if not 'athlete' in request.session: request.session['athlete'] = stravaapi.getAthlete(request.session['access_token'])
            request.session.modified = True
            return True
    return False


def get_bike_choices(request):
    bikechoices = []
    for b in request.session['athlete']['bikes']:
        bikechoices.append((b['id'], b['nickname']))        
    return bikechoices