from django.shortcuts import render, redirect
from django.forms import modelformset_factory
from django.contrib.auth.decorators import login_required

from main.views import checkbearer, index, get_bike_choices

from .models import Reminder
from .forms import ReminderForm, ReminderFormSet

# Create your views here.

@login_required
def maintenance(request):

    if not checkbearer(request): return redirect(index)
    
    MaintenanceFormSet = modelformset_factory(Reminder, formset=ReminderFormSet, form=ReminderForm, extra=1, can_delete=True)

    if request.method == 'POST':
        formset = MaintenanceFormSet(request.user.profile, request.POST, request.FILES)
        if formset.is_valid():
             formset.save()
             formset = MaintenanceFormSet(request.user.profile)

    else:
        formset = MaintenanceFormSet(request.user.profile)
        
          
    return render(request, 'maintenance/maintenance.html', {'formset': formset})
