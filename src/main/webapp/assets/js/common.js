function ajax(form) {
    const post_url = $(form).attr("action"); //get form action url
    const request_method = $(form).attr("method"); //get form GET/POST method
    const form_data = new FormData(form); //Creates new FormData object
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

setInterval(calPageReload, 30000);

function calPageReload(){
    if($('.calendar').length > 0){
        pageReplace(window.location);
    }
}

function pageReplace(url) {
    $.get(url)
        .done(function (response) {
            const doc = new DOMParser().parseFromString(response, 'text/html');
            $('.document').replaceWith($(doc).find('.document'));
            setTimeMark();
            history.replaceState({}, null, url);
        });
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
$(document).on('click', '.j_cell', function (e) {
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
                defineProjectSelect();
            })
    }
})

function defineProjectSelect(){
    const select = $('#groupId');
    select.select2({
        width: '100%',
        ajax: {
            url: select.data('url'),
            dataType: 'json',
            delay: 150,
            data: function (params) {
                return {
                    name: params.term // search term
                };
            },
            processResults: function (data) {
                return {
                    results: $.map(data.items, function (item) {
                        return {
                            text: item.name,
                            id: item.id
                        }
                    })
                };
            },
            cache: true
        },
        placeholder: 'Search for a repository',
        minimumInputLength: 3
    });
}

// Validate new item form
$(document).on('input, change', '.item-form .form_row .form_row_rc input, .item-form .form_row .form_row_rc select, .item-form .form_row .form_row_rc textarea', function () {
    const form = $(this).closest('form')
    const button = form.closest('.toggle-content').find('.toggle-submit-btn')
    $('#error-section').fadeOut(300);
    if(form.get(0).checkValidity()){
        button.prop('disabled', false)
    } else {
        button.prop('disabled', true);
    }
})

// Switch between projectId and title input
$(document).on('change', '.j-calendar-item-form #type', function () {
    if(this.value === 'PROJECT'){
        $('#groupId').attr('disabled', false).closest('.form_row').removeClass('hidden');
        $('#title').attr('disabled', true).closest('.form_row').addClass('hidden');
        $('#editItem input[name="groupId"][type="hidden"]').attr('disabled', true);
    } else {
        $('#groupId').attr('disabled', true).closest('.form_row').addClass('hidden');
        $('#title').attr('disabled', false).closest('.form_row').removeClass('hidden');
        $('#editItem input[name="groupId"][type="hidden"]').attr('disabled', false);
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
            const responsibleManagerSelect = $('#responsibleManager');
            const responsibleManager = parseInt(responsibleManagerSelect.val());
            let url = new URLSearchParams(window.location.search);
            if(!isNaN(responsibleManager) && responsibleManager !== parseInt(form.find('select[name="managerId"]').val()))  url.delete('responsibleManager');
            url.set('date', response.calendarDate);
            url = window.location.pathname + '?' + url.toString();
            pageReplace(url);
            $('.toggle').remove();

        }).fail(function (xhr) {
            const json = JSON.parse(xhr.responseText);
            $('#error-section').empty().append(json.message).fadeIn(150);
            button.prop('disabled', true);
    })
})

// Update calendar by changing date
$(document).on('change', '#calendarDate', function () {
    let url = new URLSearchParams(window.location.search);
    url.set('date', $(this).val());
    url = window.location.pathname + '?' + url.toString();
    pageReplace(url);
})

// Update calendar by changing manager
$(document).on('change', '#responsibleManager', function () {
    let url = new URLSearchParams(window.location.search);
    if(!$(this).val()) url.delete('responsibleManager');
    else url.set('responsibleManager', $(this).val());
    url = window.location.pathname + '?' + url.toString();
    pageReplace(url);
})

// Change calendar date by clicking next/back buttons
$(document).on('click', '.next,.back', function () {
    let url = new URLSearchParams(window.location.search);
    url.set('date', $(this).data('target-date'));
    url = window.location.pathname + '?' + url.toString();
    pageReplace(url);
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
                pageReplace(window.location);
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
            pageReplace(window.location);
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
            defineProjectSelect();
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
            pageReplace(window.location);
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
    const form = $('#reportForm');
    if(checkbox.is(":checked")){
        form.attr('action', checkbox.data('url'));
        form.trigger('submit');
        return;
    }
    ajax(form.get(0))
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