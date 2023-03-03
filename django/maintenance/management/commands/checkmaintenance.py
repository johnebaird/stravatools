import logging

from django.core.management.base import BaseCommand
from django.core.mail import send_mail
from django.utils import timezone

from maintenance.models import Reminder
from main import stravaapi
from main.models import Logging

logger = logging.getLogger(__name__)

class Command(BaseCommand):

    help = "task to check for and send bike maintenance emails"

    def handle(self, *args, **kwargs):
        logger.info("running bike maintenance email check")

        reminders = Reminder.objects.all().order_by('profile')
        current_profile = None
                
        for rem in reminders:

            if current_profile != rem.profile:
                stravaapi.refreshBearer(rem.profile.bearer)
                access_token = rem.profile.bearer.access_token
                athlete = stravaapi.getAthlete(access_token)
                current_profile = rem.profile

            for bike in athlete['bikes']:
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
                        Logging.objects.create(datetime=timezone.now(), profile=rem.profile, 
                                               message="Sent maintenance email to " + str(rem.email) + ": " + subject + " " + rem.message,
                                               application='maintenance')
                        



            

            


        
                   
            