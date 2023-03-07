from . import stravaapi

# check bearer token, populate from user data if user is logged in and set in session
# otherwise make sure bearer is in session data for users without accounts and if not redirect
def checkbearer(request) -> bool:
    if request.user.is_authenticated and request.user.profile.bearer:
        newbearer = stravaapi.refreshBearer(request.user.profile.bearer.expires_at,request.user.profile.bearer.refresh_token)
        if newbearer:
            request.user.profile.bearer.load_json(newbearer)
            request.user.profile.bearer.save()

        request.session['access_token'] = request.user.profile.bearer.access_token
        request.session['stravawrite'] = request.user.profile.bearer.write_access
        if not 'athlete' in request.session: request.session['athlete'] = stravaapi.getAthlete(request.session['access_token'])
        request.session.modified = True
        return True
    else:
        if 'bearer' in request.session:
            newbearer = stravaapi.refreshBearer(request.session['bearer']['expires_at'], request.session['bearer']['refresh_token'])
            if newbearer:
                request.session['bearer'] = newbearer

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