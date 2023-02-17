from django.db import models
from django.contrib.auth.models import User
from django.db.models.signals import post_save
from django.dispatch import receiver

# Create your models here.

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
    
class Bike(models.Model):
    id = models.CharField(max_length=20, primary_key=True)
    primary = models.BooleanField(default=False)
    name = models.CharField(max_length=100)
    distance = models.IntegerField(blank=True, null=True)
    brand_name = models.CharField(max_length=100)
    model_name = models.CharField(max_length=100)
    description = models.CharField(max_length=200)
    
class Profile(models.Model):
    user = models.OneToOneField(User, on_delete=models.CASCADE)
    autochange_indoor_bike = models.BooleanField(default=False)
    autochange_outdoor_bike = models.BooleanField(default=False)
    bearer = models.ForeignKey(Bearer, on_delete=models.CASCADE, blank=True, null=True)
    indoor_bike = models.ForeignKey(Bike, on_delete=models.CASCADE, related_name='indoor_bike', blank=True, null=True)
    outdoor_bike = models.ForeignKey(Bike, on_delete=models.CASCADE, related_name='outdoor_bike', blank=True, null=True)

@receiver(post_save, sender=User)
def create_user_profile(sender, instance, created, **kwargs):
    if created:
        Profile.objects.create(user=instance)

@receiver(post_save, sender=User)    
def save_user_profile(sender, instance, **kwargs):
    instance.profile.save()
    
