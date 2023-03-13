from django.db import models
from main.models import Bike

# Create your models here.

class DefaultBike(models.Model):
    autochange_indoor_bike = models.BooleanField(default=False)
    autochange_outdoor_bike = models.BooleanField(default=False)
    indoor_bike = models.ForeignKey(Bike, on_delete=models.SET_NULL, related_name='indoor_bike', blank=True, null=True)
    outdoor_bike = models.ForeignKey(Bike, on_delete=models.SET_NULL, related_name='outdoor_bike', blank=True, null=True)

    def __str__(self):
        if self.profile:
             return str(self.profile) + " Defaults"
        else:
            return super.__str__

        