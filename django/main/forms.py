from django import forms
from crispy_forms.helper import FormHelper
from crispy_forms.layout import Layout, Fieldset, HTML

class UpdatableActivity(forms.Form):
    commute = forms.BooleanField(required=False)
    trainer = forms.BooleanField(required=False)
    hide_from_home = forms.BooleanField(required=False, label="Hide from Feed")
    description = forms.CharField(required=False, label='description', max_length=500)
    name = forms.CharField(label='name', max_length=100)
    gear_id = forms.ChoiceField(label="Gear:")
    
    def __init__(self, *args, **kwargs):
        super().__init__(*args, **kwargs)
        self.helper = FormHelper()
        self.distance = 0
        self.sport_type = ""
        self.helper = FormHelper()
        self.helper.layout = Layout(
            Fieldset("",
                     'name',
                     'description',
                     HTML('<div class="mb-3"> Distance: {{ form.distance }} </div>'),
                     HTML('<div class="mb-3"> Sport Type: {{ form.sport_type}} </div>'),
                     'commute',
                     'trainer',
                     'hide_from_home',
                     'gear_id')
        )

