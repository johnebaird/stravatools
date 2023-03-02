from django.db import models
from main.models import Profile
from main.models import Bike

# Create your models here.

class Reminder(models.Model):
    profile = models.ForeignKey(Profile, on_delete=models.CASCADE)
    bike = models.ForeignKey(Bike, on_delete=models.CASCADE)
    email = models.EmailField()
    message = models.CharField(max_length=500)
    every = models.IntegerField()
    last_sent = models.IntegerField(blank=True, null=True)