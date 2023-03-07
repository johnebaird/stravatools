import requests
import os
import logging
import json

from .models import Bearer
from main.models import Bike

from datetime import datetime, time, timezone

logger = logging.getLogger(__name__)

STRAVA_CLIENT_ID = os.getenv("STRAVA_CLIENT_ID")
STRAVA_CLIENT_SECRET = os.getenv("STRAVA_CLIENT_SECRET")

stravaurl = "https://www.strava.com/api/v3"

def refreshBearer(expires_at: int, refresh_token:str) -> dict:
    
    if (round(datetime.utcnow().timestamp()) + 3500) < int(expires_at):
        return None
    
    payload = {'client_id': STRAVA_CLIENT_ID, 
                'client_secret': STRAVA_CLIENT_SECRET, 
                'grant_type': 'refresh_token',
                'refresh_token': refresh_token}

    r = requests.post(stravaurl + "/oauth/token", data=payload)

    logger.debug("Post to refresh Bearer token returned: %s", r.text)

    returned_data = json.loads(r.text)

    if 'errors' in returned_data:
        logger.error("Error refreshing Bearer token")
        return 
    
    return returned_data


def getBearerFromCode(code: str) -> Bearer:
    
    logger.info("Querying Strava API for bearer code..")

    logger.debug("Using Strava Client: %s", STRAVA_CLIENT_ID)
    logger.debug("Using Strava Client Secret: %s", STRAVA_CLIENT_SECRET)
    
    payload = {'client_id': STRAVA_CLIENT_ID, 
                'client_secret': STRAVA_CLIENT_SECRET, 
                'code': code, 
                'grant_type': 'authorization_code'}

    r = requests.post(stravaurl + "/oauth/token", data=payload)

    logger.debug("Post for Bearer Code returned: %s", r.text)

    returned_data = json.loads(r.text)

    if 'errors' in returned_data:
        logger.error("Error querying for Bearer Token")
        return None

    return returned_data

def getAthlete(access_token: str) -> str:
    logger.info("Querying for Athelete..")

    headers = {"Authorization" : "Bearer " + access_token}

    logger.debug("using access_token: %s", headers)

    r = requests.get(stravaurl + "/athlete", headers=headers)

    logger.debug("GET for Athelete info returned: %s", r.text)

    return json.loads(r.text)

def changeActivityDateRange(access_token: str, after: datetime, before: datetime, bike: dict, change: str) -> list:
    logger.info("Changing activity for date range")

    results = []
    results.append("Querying strava for activities between " + str(after) + " and " + str(before))

    # magic datetime stuff
    before = round(datetime.combine(before, time(0,0,0), tzinfo=timezone.utc).timestamp())
    after = round(datetime.combine(after, time(23,59,59), tzinfo=timezone.utc).timestamp())

    headers = {"Authorization" : "Bearer " + access_token}
    params = {"page": 1, 
              "before": before, 
              "after": after}
    
    while True:

        activities = requests.get(stravaurl + "/athlete/activities", headers=headers, params=params)
        
        if activities.status_code == 200:
            activities = json.loads(activities.text)
            results.append("checking " + str(len(activities)) + " activities on page " + str(params['page']))
            pageresults = changeActivitiesPage(access_token, activities, bike, change)
            results = results + pageresults
            results.append("changed " + str(len(pageresults)) + " activities on page " + str(params['page']))
        else:
            logger.error("Activity query returned %i %s", activities.status_code, activities.text)
            results.append("activity query returned error")
            break;
        
        # should get 30 activities a page, if we get less there are no more pages
        if len(activities) < 30: 
            results.append("checked all activities")
            break

        # failsafe, shouldn't need to do this many pages
        if params['page'] == 50: break

        params['page'] += 1
        
    
    return results


        
def changeActivitiesPage(access_token: str, activities: json, bike: dict, change: str) -> list:

    results = []
    if change == 'indoor':
        for activity in activities:
            # any activity with trainer=true (and is a Ride or VirtualRide) or a type of VirtualRide is an indoor ride
            if (activity['trainer'] and (activity['sport_type'] == 'Ride' or activity['sport_type'] == 'VirtualRide')) \
                or activity['sport_type'] == 'VirtualRide':
                            
                if activity['gear_id'] != bike['id']:
                    logger.info("updating indoor activity %s to bike %s", activity['id'], bike['name'])
                    results.append("Updating activity " + str(activity['id']) + " to bike " + bike['name'])
                    updateActivity(access_token, int(activity['id']), {'gear_id': bike['id']})
    
    if change == 'outdoor':
        for activity in activities:
            # outdoor rides should have BOTH trainer=false and sport_type of Ride
            if (not activity['trainer'] and activity['sport_type'] == 'Ride'):
                
                if activity['gear_id'] != bike['id']:
                    logger.info("updating outdoor activity %s to bike %s", activity['id'], bike['name'])
                    results.append("Updating activity " + str(activity['id']) + " to bike " + bike['name'])
                    updateActivity(access_token, int(activity['id']), {'gear_id': bike['id']})
    
    return results

def getActivities(access_token: str, page: int) -> dict:
    logger.info("Querying for athlete activities")

    headers = {"Authorization" : "Bearer " + access_token}
    params = {"page": page}

    logger.debug("using access_token: %s", headers)

    r = requests.get(stravaurl + "/activities", headers=headers, params=params)

    return json.loads(r.text)

def getActivityFromId(access_token: str, id: int) -> dict:
    logger.info("Querying for activity %s", id)

    headers = {"Authorization": "Bearer " + access_token}
    
    logger.debug("using access_token: %s", headers)
    r = requests.get(stravaurl + "/activities/" + str(id), headers=headers)

    data = json.loads(r.text)
    del data['map']
    logger.debug("GET for specific activity returned %s", data)

    return data

def getGear(access_token:str) -> dict:
    logger.info("Querying for gear..")

    headers = {"Authorization": "Bearer " + access_token}

    r = requests.get(stravaurl + "/gear", headers=headers)

    logger.debug("Gear query returns: %s", r.text)

    return json.loads(r.text)


def updateActivity(access_token: str, id:int, data:dict) -> None:
    logger.info("Updating activity %s", id)
    logger.debug(" with: %s", data)

    headers = {"Authorization": "Bearer " + access_token}

    r = requests.put(stravaurl + "/activities/" + str(id), data=data, headers=headers)

    logger.debug("Activity update PUT returns: %s", r.status_code)
    return