from django import forms
from .models import DefaultBike, Bike

class DefaultBikesForm(forms.ModelForm):

    indoor_bike = forms.ChoiceField()
    outdoor_bike = forms.ChoiceField()
    
    class Meta:
        model = DefaultBike
        fields = ['autochange_indoor_bike', 'autochange_outdoor_bike']