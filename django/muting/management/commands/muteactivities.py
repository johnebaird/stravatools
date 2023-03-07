import logging

from django.core.management.base import BaseCommand
from django.utils import timezone

from muting.models import Muting
from main import stravaapi
from main.models import Logging

logger = logging.getLogger(__name__)

class Command(BaseCommand):
    help = "task for muting activities"

    def handle(self, *args, **kwargs):
        logger.info("Running activity muting task")

        mutingtasks = Muting.objects.all().order_by('profile')
        current_profile = None

        for muting in mutingtasks:
            logger.info("Checking muting activity %s", muting)

            if current_profile != muting.profile:
                newbearer = stravaapi.refreshBearer(muting.profile.bearer.expires_at, muting.profile.bearer.refresh_token)
                if newbearer:
                    muting.profile.bearer.load_json(newbearer)
                    muting.profile.bearer.save()

                access_token = muting.profile.bearer.access_token
                activities = stravaapi.getActivities(access_token, 1)
                current_profile = muting.profile
            
            # annoyingly Stravas query for activities doesn't include hide_from_home aka muting in the data
            # so we record the earliest activity changed in each run and quit when we get there
            # so we're only looking at 'new' activities
            firstchanged = False
            for activity in activities:

                if activity['id'] == muting.lastupdatedactivity:
                    break

                if activity['sport_type'] == muting.activitytype:
                    if muting.muteall or activity['elapsed_time'] < (muting.duration * 60):
                        
                        stravaapi.updateActivity(access_token, activity['id'], {'hide_from_home': True})

                        details = str(activity['id']) + " " + activity['name'] + " - " + activity['sport_type'] + " " + str(activity['elapsed_time']/60) + " minutes long"
                        
                        logger.info("Muted activity %s", details)
                        
                        if not firstchanged:
                            muting.lastupdatedactivity = activity['id']
                            muting.save()
                            firstchanged = True
                        
                        Logging.objects.create(datetime=timezone.now(), profile=muting.profile, 
                                               message="Muted activity " + details,
                                               application='muting')

                        

                        




