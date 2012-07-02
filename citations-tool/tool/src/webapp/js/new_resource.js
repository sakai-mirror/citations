/*******************************************************************
 * Process a click on a link to modify citations in a citation list.
 * Links can trigger a search, a citation editor or an import page.
 * This function will check whether the context is creating a new 
 * citation list or revising an existing citation list. If it's 
 * creating a new citation list, the function checks whether a name
 * has been entered and requires an input before proceeding.  If a
 * name has been entered, the function posts an AJAX request to ensure
 * that a ContentResource and CitationCollection has been created. 
 * If that succeeds, this function makes this function call:
 * 
 * 		createSuccess.invoke(jsObj)
 * 
 * where jsObj is a Javascript object returned by the successful AJAX 
 * call.  The jsObj object has name-value pairs, including jsObj.message,
 * jsObj.collectionId, jsObj.resourceId, and jsObj.citationCollectionId. 
 * If the AJAX request fails, this function makes this function call:
 * 
 * 		failureFunction.invoke(jqXHR, textStatus, errorThrown)
 * 
 * where the parameters are the same as those described for the error 
 * function in jQuery's ajax function (http://api.jquery.com/jQuery.ajax/). 
 * If the context is revising an existing citation list rather than creating
 * a new citation list, this function makes this function call:
 * 
 * 		modifySuccess.invoke(jsObj)
 * 
 * where jsObj is a Javascript object with name-value pairs, including 
 * jsObj.collectionId, jsObj.resourceId, and jsObj.citationCollectionId. 
 *******************************************************************/

// assume jquery

// create citations_new_resource namespace if it doesn't exist
var citations_new_resource = citations_new_resource || {};

/*
 * used in the json returned by actions that
 * need to be notified to the user 
 */
var reportSuccess = function(msg){
    $('#messagePanel').html(msg).fadeTo("slow", 1).animate({
        opacity: 1.0
    }, 5000).fadeTo(3000, 0);
};

/*
 * There has been an error
 */
var reportError = function(msg){
    $('#messageError').html(msg).fadeTo("slow", 1).animate({
        opacity: 1.0
    }, 5000).fadeTo(3000, 0);
};

function resizeFrame(updown) {
	if (top.location != self.location) 	 {
		var frame = parent.document.getElementById(window.name);
	}	
	if( frame ) {
		var clientH = document.body.clientHeight;
		if(updown != 'shrink') {
			clientH += 30;
		}
		$( frame ).height( clientH );
	} else {
//		throw( "resizeFrame did not get the frame (using name=" + window.name + ")" );
	}
}

citations_new_resource.setupToggleAreas = function(toggler, togglee, openInit, speed){
	// toggler=class of click target
	// togglee=class of container to expand
	// openInit=true - all togglee open on enter
	// speed=speed of expand/collapse animation
	if (openInit == true && openInit != null) {
		$('.expand').hide();
	}
	else {
	    $('.' + togglee).hide();
	    $('.collapse').hide();
	    resizeFrame();
	}
	$('.' + toggler).click(function(){
	    $(this).next('.' + togglee).fadeToggle(speed);
	    $(this).find('.expand').toggle();
	    $(this).find('.collapse').toggle();
	    resizeFrame();
	});
}


citations_new_resource.processClick = function(successAction) {
	var requestDisplayName = function() {
		// TODO: use sakai message in DOM
		reportError('Please supply a name for the citation list.');
		return;
	};
	var postAjaxRequest = function(params, successAction) {
		var actionUrl = $('#newCitationListForm').attr('action');
		$.ajax({
			type		: 'POST',
			url			: actionUrl,
			cache		: false,
			data		: params,
			dataType	: 'json',
			success		: function(jsObj) {
				$.each(jsObj, function(key, value) {
					if(key === 'message' && value && 'null' !== value && '' !== $.trim(value)) {
						reportSuccess(value);
					} else if($.isArray(value)) {
						reportError('result for key ' + key + ' is an array: ' + value);
					} else {
						$('input[name=' + key + ']').val(value);
					}
				});
				if(successAction && successAction.invoke) {
					successAction.invoke(jsObj);
				}
			},
			error		: function(jqXHR, textStatus, errorThrown) {
				// TODO: replace with reasonable error handling
				reportError("failed: " + textStatus + " :: " + errorThrown);
			}
		});
		
	};
	var handleNewResource = function(successAction) {
    	$('.citation_action').val('create_resource');
    	$('.requested_mimetype').val('application/json');
    	$('.ajaxRequest').val('true');
		var params = $('#newCitationListForm').find('input').serializeArray();
		postAjaxRequest(params, successAction);
	};
	var handleExistingResource = function(successAction) {
    	$('.citation_action').val('update_resource');
    	$('.requested_mimetype').val('application/json');
    	$('.ajaxRequest').val('true');
        var newValues = $('#newCitationListForm').find('input').serializeArray();
        var newValuesObj = {};
        var oldValues = $('#fossils').find('input').serializeArray();
        var oldValuesObj = {};
        $.each(newValues, function(index, obj) {
        	if(newValuesObj[obj.name]) {
        		newValuesObj[obj.name].push(obj.value);
        	} else {
        		newValuesObj[obj.name] = [ obj.value ];
        	}
        });
        $.each(oldValues, function(index, obj) {
        	if(oldValuesObj[obj.name]) {
        		oldValuesObj[obj.name].push(obj.value);
        	} else {
        		oldValuesObj[obj.name] = [ obj.value ];
        	}
        });
        var changes = false;
        $.each(newValuesObj, function(key, value) {
            if(! oldValuesObj[key] || oldValuesObj[key].length != value.length) {
                newValues.push({ 'name':'resource_changes', 'value':key });
                changes = true;
            } else if(value.length > 1) {
                var differences = false;
                value.sort();
                if(oldValuesObj[key] && oldValuesObj[key].length > 1) {
                    oldValuesObj[key].sort();
                }
                $.each(value,function(i,v){
                    if(oldValuesObj[key] && oldValuesObj[key][i] && oldValuesObj[key][i] === v) {
                        // do nothing
                    } else {
                        differences = true;
                        return;
                    }
                });
                if(differences) {
                	newValues.push({ 'name':'resource_changes', 'value':key });
                	changes = true;
                }
            } else if(value.length > 0) {
                if(value[0] !== oldValuesObj[key][0]) {
                	newValues.push({ 'name':'resource_changes', 'value':key });
                	changes = true;
                }
            }
        	//alert('newValuesObj[' + key + '] == ' + value + '\noldValuesObj[' + key + '] == ' + oldValuesObj[key] + '\n' + changes);
        });
        if(changes) {
        	postAjaxRequest(newValues, successAction);
        } else {
            var jsObj = {};
			if(successAction && successAction.invoke) {
				successAction.invoke(jsObj);
			}
        }
	};
	var displayName = $('#displayName').val();
	// TODO: consider chacking for #displayName_fossil and using it?
	// var oldDisplayName = $('#displayName_fossil').val();
	if(displayName && '' !== $.trim(displayName)) {
		var resourceId = $('#resourceId').val();
		if(resourceId) {
			// are there changes?
			handleExistingResource(successAction);
		} else {
			// create a new resource
			handleNewResource(successAction);
		}
	} else {
		// demand a displayName and stay on page
		requestDisplayName();
	}
	
};


citations_new_resource.init = function() {
	var DEFAULT_DIALOG_HEIGHT = 610;
	var DEFAULT_DIALOG_WIDTH = 850;
	var setFrameHeight = function() {
		var body_height = $('body').innerHeight() - 100;
	    if(body_height < DEFAULT_DIALOG_HEIGHT) {
	        var spacer_height = DEFAULT_DIALOG_HEIGHT - body_height;
	        $('body').append('<div style="height:' + spacer_height + 'px; min-height:' + spacer_height + 'px;"></div>');
	        setMainFrameHeight( window.name );
	    }		
	};
	
	var childWindow = {};
	
	$('.saveciteClient a').click(function(eventObject) {
		var successObj = {
			linkId				: $(eventObject.target).attr('id'),
			saveciteClientUrl	: $(eventObject.target).siblings('.saveciteClientUrl').text(),
			popupTitle			: $(eventObject.target).siblings('.popupTitle').text(),
			windowHeight		: $(eventObject.target).siblings('.windowHeight').text(),
			windowWidth			: $(eventObject.target).siblings('.windowWidth').text(),
			invoke				: function(jsObj) {
				if(childWindow && childWindow[this.linkId] && childWindow[this.linkId].close) {
					childWindow[this.linkId].close();
				}
				childWindow[this.linkId] = openWindow(this.saveciteClientUrl,this.popupTitle,'scrollbars=yes,toolbar=yes,resizable=yes,height=' + this.windowHeight + ',width=' + this.windowWidth);
				childWindow[this.linkId].focus();
			}
		};
		citations_new_resource.processClick(successObj)
	});
	$('#Search').click(function(eventObject) {
		var successObj = {
			linkId				: $(eventObject.target).attr('id'),
			searchUrl			: $(eventObject.target).siblings('.searchUrl').text(),
			popupTitle			: $(eventObject.target).siblings('.popupTitle').text(),
			invoke				: function(jsObj) {
				try {
					if(jsObj && jsObj.resourceId) {
						searchUrl += "&resourceId=" + jsObj.resourceId;
					}
					if(jsObj && jsObj.citationCollectionId) {
						searchUrl += "&citationCollectionId=" + jsObj.citationCollectionId;
					}
				} catch (e) {
					reportError(e);
				}
				if(childWindow && childWindow[this.linkId] && childWindow[this.linkId].close) {
					childWindow[this.linkId].close();
				}
				childWindow[this.linkId] = openWindow(this.searchUrl,this.popupTitle,'scrollbars=yes,toolbar=yes,resizable=yes,height=' + DEFAULT_DIALOG_HEIGHT + ',width=' + DEFAULT_DIALOG_WIDTH);
				childWindow[this.linkId].focus();
			}
		};
		citations_new_resource.processClick(successObj)
	});
	$('#SearchGoogle').click(function(eventObject) {
		var successObj = {
			linkId				: $(eventObject.target).attr('id'),
			googleUrl			: $(eventObject.target).siblings('.googleUrl').text(),
			popupTitle			: $(eventObject.target).siblings('.popupTitle').text(),
			invoke				: function(jsObj) {
				if(childWindow && childWindow[this.linkId] && childWindow[this.linkId].close) {
					childWindow[this.linkId].close();
				}
				childWindow[this.linkId] = openWindow(this.googleUrl,this.popupTitle,'scrollbars=yes,toolbar=yes,resizable=yes,height=' + DEFAULT_DIALOG_HEIGHT + ',width=' + DEFAULT_DIALOG_WIDTH);
				childWindow[this.linkId].focus();
			}
		};
		citations_new_resource.processClick(successObj)
	});
	$('#CreateCitation').click(function(eventObject) {
		var successObj = {
			invoke				: function(jsObj) {
				$('#sakai_action').val('doCreate');
				$('#ajaxRequest').val('false');
				$('#newCitationListForm').submit();
			}
		};
		citations_new_resource.processClick(successObj)
	});
	$('#ImportCitation').click(function(eventObject) {
		var successObj = {
			invoke				: function(jsObj) {
				$('#sakai_action').val('doImportPage');
				$('#ajaxRequest').val('false');
				$('#newCitationListForm').submit();
			}
		};
		citations_new_resource.processClick(successObj)
	});
	$('.Done').click(function(eventObject) {
		var successObj = {
			invoke				: function(jsObj) {
				$('#sakai_action').val('doFinish');
				$('#ajaxRequest').val('false');
				$('#newCitationListForm').attr('method', 'GET');
				$('#newCitationListForm').submit();
			}
		};
		citations_new_resource.processClick(successObj)
	});
	$('.Cancel').click(function(eventObject) {
		var successObj = {
			invoke				: function(jsObj) {
				$('#sakai_action').val('doCancel');
				$('#ajaxRequest').val('false');
				$('#newCitationListForm').attr('method', 'GET');
				$('#newCitationListForm').submit();
			}
		};
		citations_new_resource.processClick(successObj)
	});
	$('#access_mode_groups').change(function(eventObject) {
		$('#groupTable').toggle();
	});
	$(window).unload(function() {
		if(childWindow) {
			for (key in childWindow) {
				if(childWindow[key] && childWindow[key].close) {
					childWindow[key].close();
				}
			}
		}
	});
	$('#hideAccess, #showAccess').click(function(eventObject){
		$('#accessShown').toggle();
		$('#accessHidden').toggle();
		setFrameHeight();
	});
	// If changes are saved, "Done" button should be disabled and "Cancel" button should be enabled
	// If changes are not saved, "Done" button should be enabled and "Cancel" button should be disabled
	$('form').find('input').change(function(eventObject){
		
		// if values of input elements in form have changed since save, enable "Cancel" button and disable "Done" button
	});
	
	setFrameHeight();

};

$(document).ready(function(){
	citations_new_resource.init();
	citations_new_resource.setupToggleAreas('toggleAnchor', 'toggledContent', false, 'fast');
	
});

