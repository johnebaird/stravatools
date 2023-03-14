import logging

from django.shortcuts import render, redirect
from django.forms import modelformset_factory
from django.contrib.auth.decorators import login_required

from main.utils import checkbearer
from main.models import Logging

from .models import Reminder
from .forms import ReminderForm, ReminderFormSet

# Create your views here.

logger = logging.getLogger(__name__)

@login_required
def maintenance(request):

    if not checkbearer(request): return redirect("main:index")
    changelog = Logging.objects.filter(profile=request.user.profile, application='maintenance').order_by('datetime')[:10]
    
    MaintenanceFormSet = modelformset_factory(Reminder, formset=ReminderFormSet, form=ReminderForm, extra=1, can_delete=True)

    if request.method == 'POST':
        formset = MaintenanceFormSet(request.user.profile, request.POST, request.FILES)
        if formset.is_valid():
             logger.debug("maintenance formset is valid")
             
             for form in formset:
                 if form.is_valid() and 'bike' in form.cleaned_data:
                    if form.instance.last_sent is None:
                        form.instance.last_sent = form.instance.bike.distance
                    form.save()

             formset.save()
             formset = MaintenanceFormSet(request.user.profile)

    else:
        formset = MaintenanceFormSet(request.user.profile)
        
          
    return render(request, 'maintenance/maintenance.html', {'formset': formset, 'changelog': changelog})
