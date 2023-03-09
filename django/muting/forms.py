from django.forms import ModelForm, BaseModelFormSet

from .models import Muting

from crispy_forms.helper import FormHelper

class MutingFormSet(BaseModelFormSet):
    def __init__(self, profile, *args, **kwargs):
        super().__init__(*args, **kwargs)
        self.queryset = Muting.objects.filter(profile=profile)
        self.form_kwargs = {'profile': profile}
        self.helper = FormHelper()
        self.helper.template = 'bootstrap5/table_inline_formset.html'

class MutingForm(ModelForm):

    def __init__(self, profile, *args, **kwargs):
        super(MutingForm, self).__init__(*args, **kwargs)
        self.instance.profile = profile

    class Meta:
        model = Muting
        fields = ['activitytype', 'duration', 'muteall',]



