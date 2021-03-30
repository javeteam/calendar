function ajax(form) {
    var post_url = $(form).attr("action"); //get form action url
    var request_method = $(form).attr("method"); //get form GET/POST method
    var form_data = new FormData(form); //Creates new FormData object
    return $.ajax({
        url : post_url,
        type: request_method,
        data : form_data,
        contentType: false,
        cache: false,
        processData:false,
    });
}

function ajax_json(form) {
    return $.ajax({
        url : $(form).attr("action"),
        type: $(form).attr("method"),
        data : new FormData(form),
        contentType: false,
        processData:false,
        data_type: 'json'
    });
}

// Set time mark height on page load
setTimeMark();

// Initiate page data update every 30s
autoPageReload();

// Page data update function (if no toggle page opened)
function autoPageReload(){
    const togglePageOpened = $(document).find('.toggle').length > 0;
    if(!togglePageOpened){
        let form = $(document).find('#calendarPropertiesForm').get(0);
        changeCalendarDate(form);
    }

    setTimeout(function () {
        autoPageReload();
    }, 30000);
}

function setTimeMark(){
    const calendarHeight = $('.calendar table tbody').height();
    $('.current-time-line').css('height', calendarHeight);
}

// change between duration and deadline
$(document).on('click', '.switch-link', function () {
    let section;
    let oppositeSection;
    const repetitionsSection = $('#repetitions');
    if($(this).hasClass('duration')){
        section = $(this).closest('.form_row');
        oppositeSection = $('#endDate').closest('.form_row');
        repetitionsSection.removeClass('hidden');
        repetitionsSection.find('input').prop('disabled', false);
    } else if($(this).hasClass('deadline')){
        section = $(this).closest('.form_row');
        oppositeSection = $('#duration').closest('.form_row');
        repetitionsSection.addClass('hidden');
        repetitionsSection.find('input').prop('disabled', true);
    }
    section.addClass('hidden');
    section.find('input').prop('disabled', true);
    oppositeSection.removeClass('hidden').find('input').prop('disabled', false);
})

// close toggle page
$(document).on('click', '.toggle, .toggle_close_btn', function (e) {
    if(e.target === this ){
        $('.toggle').fadeOut(150).remove();
    }
})

// Add new item clicking by cell
$(document).on('click', '.cell', function (e) {
    if(e.target === this){
        const ts = $(this).children('input').val();
        const userId = $(this).closest('tr').children('td').children('input').val();
        $('#newItemStartDate').val(ts);
        $('#newItemUserId').val(userId);

        ajax($('#newItemForm').get(0))
            .done(function (response) {
                let togglePage = new DOMParser().parseFromString(response, 'text/html');
                togglePage = $(togglePage).find('.toggle');
                $('body').append(togglePage);
                $('.toggle').fadeIn(150);
            })
    }
})

//Validate new item form
$(document).on('input', '.item-form .form_row .form_row_rc input, .item-form .form_row .form_row_rc select, .item-form .form_row .form_row_rc textarea', function () {
    const form = $(this).closest('form')
    const button = form.closest('.toggle-content').find('.toggle-submit-btn')
    $('#error-section').fadeOut(300);
    if(form.get(0).checkValidity()){
        button.prop('disabled', false)
    } else {
        button.prop('disabled', true);
    }

})

//Submit new item form
$(document).on('click', '#addItemSubmit, #editItemSubmit', function () {
    let form;
    const button = $(this);
    if(this.id === 'addItemSubmit') form =  $('#addNewItem');
    else if(this.id === 'editItemSubmit') form = $('#editItem');

    ajax_json(form.get(0))
        .done(function (response) {
            const managerSelect = $('#responsibleManager');
            const currentManager = parseInt(managerSelect.val());
            //console.log(!isNaN(currentManager) + " " + currentManager + " " + form.find('select[name="managerId"]').val());
            if(!isNaN(currentManager) && currentManager !== parseInt(form.find('select[name="managerId"]').val())) managerSelect.prop('disabled', true);
            $('#calendarDate').val(response.calendarDate);
            updateCalendarPage();
            $('.toggle').remove();

        }).fail(function (xhr) {
            const json = JSON.parse(xhr.responseText);
            $('#error-section').empty().append(json.message).fadeIn(150);
            button.prop('disabled', true);
    })
})

// Change calendar date by changing calendarPropertiesForm
$(document).on('change', '#calendarDate, #responsibleManager', function () {
    const form = $(this).closest('form').get(0);
    changeCalendarDate(form);
})

function changeCalendarDate (form){
    if(form.checkValidity()){
        updateCalendarPage(form);
    }
}

// Change calendar date by clicking next/back buttons
$(document).on('click', '.next,.back', function () {
    const dateInput = $('#calendarDate');
    let date = new Date(dateInput.val());
    if ($(this).hasClass('next')) date.setDate(date.getDate() + 1);
    else date.setDate(date.getDate() - 1);
    dateInput.val(date.toISOString().substr(0, 10));
    changeCalendarDate(dateInput.closest('form').get(0));
})

// Highlight items group
$(document).on('mouseover','.items_group', function () {
    const groupId = ($(this).children('input[name="groupId"]').val());
    $('input[name="groupId"][value=' + groupId +']').parent().children('span').addClass('highlighted');
})

// Remove highlight from items group
$(document).on('mouseleave','.items_group', function () {
    const groupId = ($(this).children('input[name="groupId"]').val());
    $('input[name="groupId"][value=' + groupId +']').parent().children('span').removeClass('highlighted');
})

// Open item info toggle
$(document).on('click', '.calendar-cell-item', function () {
    const form = $(this).find('form').get(0);
    ajax(form)
        .done(function (response) {
            let toggle = new DOMParser().parseFromString(response, 'text/html');
            toggle = $(toggle).find('.toggle-item-info');
            $('body').append(toggle);

        })
})


function updateCalendarPage(form){
    if(form === undefined) form = $('#calendarPropertiesForm').get(0);
    ajax(form)
        .done(function (response) {
            replaceDoc(response);
        })
}

function replaceDoc(response){
    let doc = new DOMParser().parseFromString(response, 'text/html');
    doc = $(doc).find('.document').contents();
    $(document).find('.document').empty().append(doc);
    setTimeMark();

}

// show delete item options
$(document).on('click', '.option .delete', function () {
    const options = $('#deleteOptions');
    if(options.length > 0) {
        $(this).closest('.option-block').find('.option').hide();
        options.removeClass('hidden');
    } else {
        const form = $('#deleteForm');
        if(confirm('Are you sure?')){
            form.find('input[name="groupId"]').prop('disabled', true);
            ajax_json(form.get(0)).always(function () {
                updateCalendarPage()
                $('.toggle').remove();
            });
        }
    }
})

// delete item or group
$(document).on('click', '#delItem, #delGroup', function () {
    const form = $('#deleteForm');
    if(confirm('Are you sure?')){
        if(this.id === 'delItem'){
            form.find('input[name="groupId"]').prop('disabled', true);
        }
        ajax_json(form.get(0)).always(function () {
            updateCalendarPage()
            $('.toggle').remove();
        });
    }
})

// show edit item options
$(document).on('click', '.option .edit', function () {
    const form = $('#editForm');
    $('.toggle').remove();
    ajax(form.get(0))
        .done(function (response) {
            let toggle = new DOMParser().parseFromString(response, 'text/html');
            toggle = $(toggle).find('.toggle');
            $('body').append(toggle);

        })

})

// Change deadline date to be same as start date if it set to readonly
$(document).on('input', '#startDate', function () {
    const endDate = $('#endDate');
    if(endDate.prop('readonly')) endDate.val($(this).val());
})


// Show split options
$(document).on('click', '.option .split', function () {
    const options = $('#splitOptions');
    $(this).closest('.option-block').find('.option').hide();
    options.removeClass('hidden');
})

// Show error and hide submit button if split time is invalid
$(document).on('input', '#splitForm input[name="splitTime"]', function () {
    if ($('#splitForm').get(0).checkValidity()){
        $('#splitSubmit').fadeIn(150);
        $('#error-section').fadeOut(50);
    } else {
        $('#splitSubmit').fadeOut(50);
        $('#error-section').fadeIn(150);
    }
})

// Submit split item form
$(document).on('click', '#splitSubmit', function () {
    const form = $('#splitForm').get(0)
    if (form.checkValidity()){
        ajax_json(form).done(function () {
            updateCalendarPage()
            $('.toggle').remove();
        });
    }
})

// Open statistic or new folder parameters toggle
$(document).on('click', '.img-button', function () {
    const form = $(this).find('form').get(0);
    ajax(form)
        .done(function (response) {
            let togglePage = new DOMParser().parseFromString(response, 'text/html');
            togglePage = $(togglePage).find('.toggle');
            $('body').append(togglePage);
            if($('#createNewFolder').length > 0){
                $('#sLang, #tLang').select2({
                    width: '100%',
                    tags: true,
                    createTag: function (params) {
                        const term = $.trim(params.term);
                        const langCode1 = new RegExp('^[A-Za-z]{2}$');
                        const langCode2 = new RegExp('^[A-Za-z]{2}_[A-Za-z]{2}$');
                        if (!langCode1.test(term) && !langCode2.test(term)) {
                            return null;
                        }

                        return {
                            id: '#' + term.toUpperCase(),
                            text: term.toUpperCase(),
                            newTag: true
                        }
                    }
                });
                $('#client').select2({
                    tags: true,
                    width: '100%',
                    createTag: function (params) {
                        const term = $.trim(params.term);
                        const clientName = new RegExp('^[0-9A-Za-zА-Яа-яіІїЇєЄ_ -]{3,}$');
                        if (!clientName.test(term)) {
                            return null;
                        }

                        return {
                            id: '#' + term,
                            text: term,
                            newTag: true
                        }
                    }
                });
                $('#workflow').select2({
                    width: '100%',
                    tags: true,
                    createTag: function (params) {
                        const term = $.trim(params.term);
                        const langCode = new RegExp('^[A-Za-z]{2,20}$');
                        if (!langCode.test(term)) {
                            return null;
                        }

                        return {
                            id: '#' + term.toUpperCase(),
                            text: term.toUpperCase(),
                            newTag: true
                        }
                    }
                });
            }
            $('.toggle').fadeIn(150);
        })
});

$(document).on('change', '#createNewFolder select', function(){
    let formValid = $(this).closest('form').get(0).checkValidity() && languagesOK();
    //($('#sLang').val() !== $('#tLang').val());
    $('#addFolderSubmit').prop('disabled', !formValid);
    $('#error-section').addClass('hidden').text();
});

function languagesOK(){
    let result = true;
    $.each($('#tLang').val(), function () {
        if($('#sLang').val().toString() === this.toString()) result = false;
    });
    return result;
}

// Show statistics preview on statistic parameters toggle submit
$(document).on('click', '#reportSubmitBtn', function () {
    $(this).prop('disabled', true);
    const checkbox = $('#itemsExport');
    if(checkbox.is(":checked")){
        const form = $('#reportForm');
        form.attr('action', checkbox.data('url'));
        form.trigger('submit');
        return;
    }
    ajax($('#reportForm').get(0))
        .done(function (response) {
            let toggle = new DOMParser().parseFromString(response, 'text/html');
            toggle = $(toggle).find('.toggle-content');
            $('.toggle-content').replaceWith(toggle);
        })
})

$(document).on('click', '#addFolderSubmit', function(){
    $(this).prop('disabled', true);
    const form = $('#createNewFolder');
    form.trigger('submit');
});

$(document).on('submit','#createNewFolder', function (e){
    e.preventDefault();
    e.stopPropagation();
    $.ajax({
        method: "POST",
        url: $(this).attr('action'),
        data: new FormData(this),
        dataType: 'json',
        contentType: false,
        cache: false,
        processData:false
    }).done(function (response) {
        $('.toggle').fadeOut(500, function () {
            $(this).remove();
            displayNotification(response);
        })
    }).fail(function (xhr) {
        const json = JSON.parse(xhr.responseText)
        $('#error-section').text(json.error).removeClass('hidden');
    });
});

// download CSV
$(document).on('click', '.option .export', function () {
    $(this).closest('div').find('form').trigger('submit');
})

// highlight row to mouseover username
$(document).on('mouseover', '.userInfo', function () {
    if($(document).find('.userInfo').length > 1){
        $(this).closest('tr').addClass('hovered');
    }
})

$(document).on('mouseleave', '.userInfo', function () {
    $(this).closest('tr').removeClass('hovered');
})

function displayNotification(jsonResponse) {
    const toggle = $('<div class="notification-toggle"/>');
    $('body').append($(toggle));
    const header = $('<div class="t-header">');
    header.append( $('<span class="description">').append(jsonResponse.status) );
    header.append( $('<div class="close_btn">'));
    const message = $('<div class="message">')
        .append('New folder for project <b>' + jsonResponse.projectName + '</b> has been created.<br>Click ')
        .append($('<a>here</a>').prop('href', 'tcmd:\'M:/' + jsonResponse.url + '\''))
        .append(' to open it in TC.');
    toggle.append(header).append(message);

    setTimeout(function () {
        $('.notification-toggle').addClass('notification-toggle-visible');
    }, 50);

}

$(document).on('click', '.notification-toggle .close_btn', function () {
    $(this).closest('.notification-toggle').removeClass('notification-toggle-visible');
    setTimeout(function () {
        $('.notification-toggle').remove();
    }, 500);
})