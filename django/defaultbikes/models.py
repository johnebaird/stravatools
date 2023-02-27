from django.db import models


# Create your models here.

class Bike(models.Model):
    id = models.CharField(max_length=20, primary_key=True)
    name = models.CharField(max_length=100, blank=True, null=True)
    profile = models.ForeignKey(to='main.Profile', on_delete=models.CASCADE)

    def __str__(self):
        if self.name:
            return f'{self.id} - {self.name}'
        return self.id


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

        