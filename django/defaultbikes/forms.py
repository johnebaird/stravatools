from datetime import datetime

from django import forms
from .models import DefaultBike, Bike

class DefaultBikesForm(forms.ModelForm):

    def __init__(self, profile, *args, **kwargs):
        super(DefaultBikesForm, self).__init__(*args, **kwargs)
        if profile is None:
            for field in self.fields:
                field.disabled = True
        else:        
            self.fields['indoor_bike'].queryset = Bike.objects.filter(profile=profile)
            self.fields['outdoor_bike'].queryset = Bike.objects.filter(profile=profile)

    class Meta:
        model = DefaultBike
        fields = ['indoor_bike', 'outdoor_bike', 'autochange_indoor_bike', 'autochange_outdoor_bike']

class DateInput(forms.DateInput):
    input_type = 'date'

def date_not_in_future(value):
    if value > datetime.now().date():
        raise forms.ValidationError('Invalid value', code='invalid')
    return value

class ManualBikeCorrection(forms.Form):
    fromdate = forms.DateField(widget=DateInput(), validators=[date_not_in_future])
    todate = forms.DateField(widget=DateInput(), validators=[date_not_in_future])
    activities = forms.ChoiceField(choices=(("indoor", "Indoor"), ('outdoor',"Outdoor")))
    bike = forms.ChoiceField()

    def clean(self):
        cleaned_data = super().clean()

        fromdate = cleaned_data.get("fromdate")
        todate = cleaned_data.get("todate")
        
        if fromdate and todate and fromdate < todate:
            msg = 'From Date must be before To Date'
            self.add_error('fromdate', msg)
            self.add_error('todate', msg)

