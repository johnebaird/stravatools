import logging

from django.core.management.base import BaseCommand
from django.core.mail import send_mail

from maintenance.models import Reminder
from main import stravaapi

logger = logging.getLogger(__name__)

class Command(BaseCommand):

    help = "task to check for and send bike maintenance emails"

    def handle(self, *args, **kwargs):
        logger.info("running bike maintenance email check")

        reminders = Reminder.objects.all()

        athletes = {}
        
        for rem in reminders:

            stravaapi.refreshBearer(rem.profile.bearer)
            access_token = rem.profile.bearer.access_token
            
            # save data from athelete since we could have multiple reminders for same
            # don't want to query API each time
            if access_token not in athletes:
                athletes[access_token] = stravaapi.getAthlete(access_token)

            for bike in athletes[access_token]['bikes']:
                if bike['id'] == rem.bike.id and bike['distance'] > rem.last_sent + (rem.every * 1000):
                    subject = bike['name'] + " at " + str(round(bike['distance'] / 1000)) + "k" + rem.message
                    sent = send_mail(subject,
                            rem.message,
                            'fromaddress@notused',
                            [rem.email],
                            fail_silently=False)
                    
                    if sent:
                        rem.last_sent = bike['distance']
                        rem.save()



            

            


        
                   
            