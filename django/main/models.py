from django.db import models
from django.contrib.auth.models import User
from django.db.models.signals import post_save
from django.dispatch import receiver
from defaultbikes.models import DefaultBike

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

    def __str__(self):
        return str(self.profile) + " - " + self.access_token
    
class Profile(models.Model):
    user = models.OneToOneField(User, on_delete=models.CASCADE)
    bearer = models.OneToOneField(Bearer, on_delete=models.SET_NULL, blank=True, null=True)
    defaultbikes = models.OneToOneField(DefaultBike, on_delete=models.SET_NULL, blank=True, null=True)

    def __str__(self):
        return self.user.username

@receiver(post_save, sender=User)
def create_user_profile(sender, instance, created, **kwargs):
    if created:
        Profile.objects.create(user=instance)

@receiver(post_save, sender=User)    
def save_user_profile(sender, instance, **kwargs):
    instance.profile.save()
    
