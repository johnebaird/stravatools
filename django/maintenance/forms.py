from django.forms import ModelForm, BaseModelFormSet

from .models import Reminder
from main.models import Bike

from crispy_forms.helper import FormHelper
from crispy_forms.layout import Submit

class ReminderFormSet(BaseModelFormSet):
    def __init__(self, profile, *args, **kwargs):
        super().__init__(*args, **kwargs)
        self.queryset = Reminder.objects.filter(profile=profile)
        self.form_kwargs = {'profile': profile}
        self.helper = FormHelper()
        self.helper.template = 'bootstrap5/table_inline_formset.html'
        self.helper.add_input(Submit('submit', 'Submit'))

class ReminderForm(ModelForm):

    def __init__(self, profile, *args, **kwargs):
        super(ReminderForm, self).__init__(*args, **kwargs)
        self.fields['bike'].queryset = Bike.objects.filter(profile=profile)
        self.instance.profile = profile
    
    class Meta:   
        model = Reminder 
        fields = ("bike", "email", "message", "every",)
        labels = {
            "bike": "Bike",
            "email": "Email",
            "messsage": "Reminder message to send",
            "every": "remind every (km)",
        }

    
