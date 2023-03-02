import logging

from django.shortcuts import render, redirect
from django.contrib.auth.decorators import login_required
from django.forms import modelformset_factory

from main.views import checkbearer, index
from .models import Muting
from .forms import MutingForm, MutingFormSet

# Create your views here.

logger = logging.getLogger(__name__)

@login_required
def muting(request):

    if not checkbearer(request): redirect(index)

    ActivityMutingFormSet = modelformset_factory(Muting, formset=MutingFormSet, form=MutingForm, extra=1, can_delete=True)

    if request.method == 'POST':

        formset = ActivityMutingFormSet(request.user.profile, request.POST, request.FILES)
        if formset.is_valid():
            logger.debug("muting form is valid")
            formset.save()
            formset = ActivityMutingFormSet(request.user.profile)
            
            
    else:
        formset = ActivityMutingFormSet(request.user.profile)

    return render(request, 'muting/muting.html', {'formset': formset})

