from django.db import models
from django.contrib.auth.models import User
from django.db.models.signals import post_save
from django.dispatch import receiver

# Create your models here.

distancechoice = [('km', 'km'), ('miles','miles')]
                 
class Bearer(models.Model):
    access_token = models.CharField(max_length=100)
    expires_at = models.IntegerField()
    expires_in = models.IntegerField()
    refresh_token = models.CharField(max_length=100)
    write_access = models.BooleanField(default=False)

    def load_json(self, json) -> None:
        self.access_token = json['access_token']
        self.expires_at = int(json['expires_at'])
        self.expires_in = int(json['expires_in'])
        self.refresh_token = json['refresh_token']

    def __str__(self):
        return str(self.profile) + " - " + self.access_token
    
class Profile(models.Model):
    user = models.OneToOneField(User, on_delete=models.CASCADE)
    bearer = models.OneToOneField(Bearer, on_delete=models.SET_NULL, blank=True, null=True)
    defaultbikes = models.OneToOneField(to='defaultbikes.DefaultBike', on_delete=models.SET_NULL, blank=True, null=True)
    distance = models.CharField(max_length=100, default='km', choices=distancechoice)

    def __str__(self):
        return self.user.username

@receiver(post_save, sender=User)
def create_user_profile(sender, instance, created, **kwargs):
    if created:
        Profile.objects.create(user=instance)

@receiver(post_save, sender=User)    
def save_user_profile(sender, instance, **kwargs):
    instance.profile.save()


class Logging(models.Model):

    # seperate out which task is logging
    appchoices = [('main', 'main'),('autobikechange', 'autobikechange'),('muting','muting'),('maintenance','maintenance')]
    
    datetime = models.DateTimeField()
    profile = models.ForeignKey(Profile, on_delete=models.CASCADE)
    application = models.CharField(max_length=25, choices=appchoices, default='main')
    message = models.CharField(max_length=500)

    def __str__(self):
        time = self.datetime.strftime("%x %X")
        return f'{time} - {self.application} : {self.message}'
    
class Bike(models.Model):
    id = models.CharField(max_length=20, primary_key=True)
    name = models.CharField(max_length=100, blank=True, null=True)
    profile = models.ForeignKey(Profile, on_delete=models.CASCADE)
    distance = models.IntegerField(default=0)

    def __str__(self):
        if self.name:
            return f'{self.id} - {self.name}'
        return self.id
    