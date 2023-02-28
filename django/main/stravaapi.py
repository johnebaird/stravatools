import requests
import os
import logging
import json

from .models import Bearer
from defaultbikes.models import Bike

from datetime import datetime, time, timezone

logger = logging.getLogger(__name__)

STRAVA_CLIENT_ID = os.getenv("STRAVA_CLIENT_ID")
STRAVA_CLIENT_SECRET = os.getenv("STRAVA_CLIENT_SECRET")

stravaurl = "https://www.strava.com/api/v3"

def refreshBearer(bearer: Bearer) -> None:
    
    if (round(datetime.utcnow().timestamp()) + 3500) < int(bearer.expires_at):
        return 
    
    payload = {'client_id': STRAVA_CLIENT_ID, 
                'client_secret': STRAVA_CLIENT_SECRET, 
                'grant_type': 'refresh_token',
                'refresh_token': bearer.refresh_token}

    r = requests.post(stravaurl + "/oauth/token", data=payload)

    logger.debug("Post to refresh Bearer token returned: %s", r.text)

    returned_data = json.loads(r.text)

    if 'errors' in returned_data:
        logger.error("Error refreshing Bearer token")
        return 
    
    bearer.load_json(returned_data)
    bearer.save()


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

    auth = {"Authorization" : "Bearer " + access_token}

    logger.debug("using access_token: %s", auth)

    r = requests.get(stravaurl + "/athlete", headers=auth)

    logger.debug("GET for Athelete info returned: %s", r.text)

    return json.loads(r.text)

def changeActivityDateRange(access_token: str, after: datetime, before: datetime, bike: str, change: str) -> None:
    logger.info("Changing activity for date range")

    # magic datetime stuff
    before = round(datetime.combine(before, time(0,0,0), tzinfo=timezone.utc).timestamp())
    after = round(datetime.combine(after, time(23,59,59), tzinfo=timezone.utc).timestamp())

    auth = {"Authorization" : "Bearer " + access_token}
    params = {"page": 1, 
              "before": before, 
              "after": after}
    
    while True:

        activities = requests.get(stravaurl + "/athlete/activities", headers=auth, params=params)
        
        if activities.status_code == 200:
            activities = json.loads(activities.text)
            changeActivitiesPage(access_token, activities, bike, change)
        else:
            logger.error("Activity query returned %i %s", activities.status_code, activities.text)
            break;
        
        # should get 30 activities a page, if we get less there are no more pages
        if len(activities) < 30: break

        # failsafe, shouldn't need to do this many pages
        if params['page'] == 50: break

        params['page'] += 1


        
def changeActivitiesPage(access_token: str, activities: json, bike: str, change: str) -> None:

    if change == 'indoor':
        for activity in activities:
            # any activity with trainer=true or a type of virtual ride is an indoor ride
            if (activity['trainer'] or activity['sport_type'] == 'VirtualRide') and activity['gear_id'] != bike:
                logger.info("updating indoor activity %s to bike %s", activity['id'], bike)
                updateActivity(access_token, int(activity['id']), {'gear_id': bike})
    
    if change == 'outdoor':
        for activity in activities:
            # outdoor rides should have BOTH trainer=false and sport_type of Ride
            if (not activity['trainer'] and activity['sport_type'] == 'Ride') and activity['gear_id'] != bike:
                logger.info("updating outdoor activity %s to bike %s", activity['id'], bike)
                updateActivity(access_token, int(activity['id']), {'gear_id': bike})

def getActivities(access_token: str, page: int) -> dict:
    logger.info("Querying for athlete activities")

    auth = {"Authorization" : "Bearer " + access_token}
    params = {"page": page}

    logger.debug("using access_token: %s", auth)

    r = requests.get(stravaurl + "/activities", headers=auth, params=params)

    return json.loads(r.text)

def getActivityFromId(access_token: str, id: int) -> dict:
    logger.info("Querying for activity %s", id)

    auth = {"Authorization": "Bearer " + access_token}
    
    logger.debug("using access_token: %s", auth)
    r = requests.get(stravaurl + "/activities/" + str(id), headers=auth)

    data = json.loads(r.text)
    del data['map']
    logger.debug("GET for specific activity returned %s", data)

    return data

def getGear(access_token:str) -> dict:
    logger.info("Querying for gear..")

    auth = {"Authorization": "Bearer " + access_token}

    r = requests.get(stravaurl + "/gear", headers=auth)

    logger.debug("Gear query returns: %s", r.text)

    return json.loads(r.text)


def updateActivity(access_token: str, id:int, data:dict) -> None:
    logger.info("Updating activity %s", id)
    logger.debug(" with: %s", data)

    auth = {"Authorization": "Bearer " + access_token}

    r = requests.put(stravaurl + "/activities/" + str(id), data=data, headers=auth)

    logger.debug("Activity update PUT returns: %s", r.status_code)
    return