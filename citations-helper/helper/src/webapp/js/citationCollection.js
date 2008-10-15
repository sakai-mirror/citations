jQuery( document ).ready(
	function() {
		citation.attachJS();
	}
);

function addValue(typeDef, index, value)
{
	var html = "";
	if("shorttext" == typeDef.valueType)
	{
		html += '<input name="' + typeDef.identifier + index + '" id="'
			+ typeDef.identifier + index + '" type="text" size="50" class="citationEditValue" ';
		if(value)
		{
			html += 'value="' + jsEscapeHtml(value) + '" ';
		}
		html += '/>\n';
	}
	else if("number" == typeDef.valueType)
	{
		html += '<input name="' + typeDef.identifier + index + '" id="'
			+ typeDef.identifier + index + '" type="text" size="20" class="citationEditValue" ';
		if(value)
		{
			html += 'value="' + jsEscapeHtml(value) + '" ';
		}
		html += '/>\n';
	}
	else if("longtext" == typeDef.valueType)
	{
		html += '<textarea name="' + typeDef.identifier + index + '" id="'
			+ typeDef.identifier + index + '" cols="50" rows="4" class="citationEditValue">';
		if(value)
		{
			html += value;
		}
		html += "</textarea>\n</p>\n";
	}
	return html;
}

function getCount(identifier)
{
	var count_item = jQuery("." + identifier + "_count");
	var count = count_item.val();
	if(typeof count == "undefined" || count < 1)
	{
		count = 1;
	}
	return count;
}

function addMultivalued(schema, typeDef, values, count)
{
	var identifier = typeDef.identifier;
		
	var html = "<div id=\"div_" + identifier + "\">\n<table class=\"multiAdded\">\n";
	for(var i = 0; i < count; i++)
	{
		var value = "";
		if(values && values.length && values.length > i)
		{
			value = values[i];
		}
		html += "<tr>\n<td>\n" + addValue(typeDef,i,value) + "</td>\n</tr>\n";
	}
	html += "<tr>\n<td>\n<a href=\"#\" onclick=\"javascript:citation.addAnother('" 
		+ schema + "', '" + typeDef.index + "');\">Add another</a>\n</td>\n</tr>\n</table>\n</div>\n"
		+ "<input type=\"hidden\" name=\"" + identifier + "_count\" id=\"" + identifier 
		+ "_count\" class=\"" + identifier + "_count\" value=\"" + count + "\" />\n";
	
	return html;
}

function getCurrentValues()
{
	var currentValues = new Array();
	var indexMatcher = new RegExp("[0-9]+$");
	var nameMatcher = new RegExp("^.*[^0-9]+");

	var objects = jQuery(".citationEditValue");
						
	for(var i = 0; i < objects.length; i++)
	{
		var obj = objects.get(i);
		var name = obj.name;
		if(name && indexMatcher.test(name))
		{
			var index = name.match(indexMatcher)[0];
			var identifier = name.match(nameMatcher)[0];
			
			if(typeof currentValues[identifier] == "undefined")
			{
				currentValues[identifier] = new Array();
			}
			
			currentValues[identifier][index] = obj.value;
		}
		else
		{
			currentValues[name] = obj.value;
		}
		
	}
		
	return currentValues;
}

// resize frame on document ready
function heavyResize() {
	var frame = parent.document.getElementById( window.name );
	
	if( frame ) {
		var clientH = document.body.clientHeight + 50;
		$( frame ).height( clientH );
	}
}

function getValue(type, t, index)
{
	var html = "";
	if("shorttext" == templates[type][t].valueType)
	{
		html += '<input name="' + templates[type][t].identifier + index + '" id="' 
				+ templates[type][t].identifier + index + '" type="text" size="50" class="citationEditValue"';
		if(currentValues[templates[type][t].identifier + index])
		{
			var str = jsEscapeHtml( currentValues[templates[type][t].identifier + index] );
			html += 'value="' + str + '" ';
		}
		html += '/>\n';
	}
	else if("number" == templates[type][t].valueType)
	{
		html += '<input name="' + templates[type][t].identifier + index + '" id="' 
				+ templates[type][t].identifier + index + '" type="text" size="20" class="citationEditValue" ';
		if(currentValues[templates[type][t].identifier])
		{
			var str = jsEscapeHtml( currentValues[templates[type][t].identifier + index] );
			html += 'value="' + str + '" ';
		}
		html += '/>\n</p>\n';
	}
	else if("longtext" == templates[type][t].valueType)
	{
		html += '<textarea name="' + templates[type][t].identifier + index + '" id="'
				 + templates[type][t].identifier + index + '" cols="50" rows="4" class="citationEditValue">'; 
		if(currentValues[templates[type][t].identifier + index])
		{
			html += currentValues[templates[type][t].identifier + index] ;
		}
		html += "</textarea>\n</p>\n";
	}
	
	return html;
}

function getMultivalued(type, t)
{
	var identifier = templates[type][t].identifier;
	var count = 1;
	var count_item = document.getElementById(identifier + "_count");
	if(count_item)
	{
	count = count_item.value;
	}
		
	var html = "<div id=\"div_" + identifier + "\">\n<table class=\"multiAdded\">\n";
	for(var i = 0; i < count; i++)
	{
	html += "<tr>\n<td>\n" + getValue(type, t, i) + "</td>\n</tr>\n";
	}
	html += "<tr>\n<td>\n<a href=\"#\" onclick=\"javascript:getAnother('" + type + "', '" + t + "');\">Add another</a>\n</td>\n</tr>\n</table>\n</div>\n"
		+ "<input type=\"hidden\" name=\"" + identifier + "_count\" id=\"" + identifier 
		+ "_count\" value=\"" + count + "\" />\n";
	
	return html;
}


function getAnother(type, t)
{
var identifier = templates[type][t].identifier;
var div = document.getElementById("div_" + identifier);
var counter = document.getElementById(identifier + "_count");
var count = 1 + parseInt(counter.value);

var html = "<table class=\"getAnother\">\n";
for(var i = 0; i < count; i++)
{
var oldElement = document.getElementById(identifier + i);
if(oldElement && oldElement.value)
{
    var str = jsEscapeHtml( oldElement.value );
	currentValues[templates[type][t].identifier + i] = str;
}
html += "<tr>\n<td>\n" + getValue(type, t, i) + "</td>\n</tr>\n";
}
html += '<tr>\n<td>\n<a href="#" onclick="javascript:getAnother(\'' + type + '\', \'' + t + '\')">Add another</a>\n</td>\n</tr>\n</table>\n';

div.innerHTML = html;
counter.value = count;

// focus the newly added item
var focusNum = String( count-1 );
document.getElementById( identifier + focusNum ).focus();

//resizeFrame();

return false;
}

function addUrl()
{
// get the current count of urls
var count = document.getElementById("url_count");
var i = parseInt( count.value );

// generate empty url datafields for one new url
var html = "<tr>\n<td>\n<label class=\"block\" for=\"url_" + i + "\">\nURL\n</label>\n"
			+ "<input name=\"url_" + i + "\" id=\"url_" + i + "\" type=\"text\" size=\"50\" class=\"citationEditValue\" />\n</td>\n</tr>\n"
			+ "<tr>\n<td>\n<label class=\"block\" for=\"pref_" + i + "\">\nUse as title link?\n</label>\n"
			+ "<input class=\"prefLink\" name=\"pref_" + i + "\" id=\"pref_" + i + "\" type=\"checkbox\" value=\"preferred\" onclick=\"javascript:trackPreferredUrl('pref_" + i + "');\" class=\"citationEditValue\" />\n</td>\n</tr>\n"
			+ "<tr>\n<td>\n<label class=\"block\" for=\"label_" + i + "\">\nLabel\n</label>\n"
			+ "<input name=\"label_" + i + "\" id=\"label_" + i + "\" type=\"text\" size=\"20\" class=\"citationEditValue\" />\n<hr />\n</td>\n</tr>\n";

// jQuery: insert new blank url datafields before the #bottomRow
$("#bottomRow").before( html );

// focus the newly added item
document.getElementById( "url_" + i ).focus();

// increment the count
count.value = String( i + 1 );

// resize the frame because we've added a new set of datafields
//resizeFrame();
}

/*
* This function is called onclick of a preferred title URL checkbox.
* It makes sure that either only one or zero links are selected as preferred.
*
* Params:
*   checkboxId  id of checkbox HTML element that has been changed
*/
function trackPreferredUrl( checkboxId )
{
// has this been clicked?
if( $( "#" + checkboxId ).attr( "checked" ) != null )
{
// clear all checkboxes
$( ".prefLink" ).attr( "checked", "" );

// re-check this one
$( "#" + checkboxId ).attr( "checked", "checked" );
}

// if the checkbox has been unchecked, we do nothing
}

function jsEscapeHtml( rawString )
{
var str = new String( rawString );
//str = str.replace(/&/g, "&amp;");
str = str.replace(/</g, "&lt;");
str = str.replace(/>/g, "&gt;");
str = str.replace(/"/g, "&quot;");
return str;
}





var citation = function() {
		
	return {
		/**
		 * This function attaches JavaScript event handlers to author.html
		 * template elements.
		 */
		attachJS : function() {
			jQuery(".chooseViewLink").click(
				function() {
					citation.switchContext("View");
				} );
			jQuery(".chooseSearchLink").click(
				function() {
					citation.switchContext("Search");
				} );
			jQuery(".chooseAddLink").click(
				function() {
					citation.switchContext("Add");
				} );
			jQuery(".chooseImportLink").click(
				function() {
					citation.switchContext("Import");
				} );
			jQuery(".citationSchemaSelector").change(
				function() {
					citation.changeSchema();
				} );
		},
		
		/**
		 */
		switchContext : function(context) {
			 // enable other links
			 jQuery("span.disabledLink span").toggle();
			 jQuery("span.disabledLink").removeClass("disabledLink").addClass("enabledLink");
			 
			 // disable link for context
			 jQuery("span.choose" + context + " span").toggle();
			 jQuery("span.choose" + context).removeClass(".enabledLink").addClass("disabledLink");
			 
			 // hide other contexts
			 jQuery(".visibleContext").removeClass("visibleContext").addClass("hiddenContext");
			 
			 // show context
			 jQuery(".citation" + context + "Context").removeClass("hiddenContext").addClass("visibleContext");
			 
			 return false;
		 },
		 
		 
		changeSchema : function () {
			
			var templates = jQuery(document).data("templates");
			var defaultTemplate = jQuery(document).data("defaultTemplate");
			var templateIdentifiers = jQuery(document).data("templateIdentifiers");

			var form_div = jQuery(".citationEditFormDiv");
			var type = jQuery(".citationSchemaSelector").val();
			
			var currentValues = getCurrentValues();
			
			var html = " ";
			
			for(var t = 0; t < templates[type].length; t++) {
				var typeDef = templates[type][t];
				
				html += '<p class="shorttext">\n';
				
				if(typeDef.isRequired) {
					html += '<span class="reqStar">*</span>\n';
					if(typeDef.maxCardinality > 1) {
						html += '<span class="requiredField_multi">\n';
					}
					else {
						html += '<span class="requiredField">\n';
					}
				}
				
				var currentValue = currentValues[typeDef.identifier];
				
				if(typeDef.maxCardinality > 1) 
				{
					var count = getCount(typeDef.identifier);

					if(typeof currentValue == "undefined")
					{
						currentValue = [""];
					}
					html += '<label for="' + typeDef.identifier + '0">\n<strong>' + typeDef.label + '</strong>\n</label>\n';
					html += addMultivalued(type, typeDef, currentValue, count);
				}
				else 
				{
					if(typeof currentValue == "undefined")
					{
						currentValue = "";
					}
					html += '<label for="' + typeDef.identifier + '">\n<strong>' + typeDef.label + '</strong>\n</label>\n';
					html += addValue(typeDef, "", currentValue);
				}
				
				if(typeDef.isRequired) {
					html += "</span>\n";
				}
				
				html += "</p>\n";
			}
			
			form_div.html( html );
			
			//resizeFrame();
		},
		
		addAnother: function (type, t)
		{
			var typeDef = templates[type][t];
			var identifier = typeDef.identifier;
			var count = getCount(identifier);

			var currentValues = getCurrentValues();
			
			var html = addMultivalued(type, typeDef, currentValues[identifier], count + 1);

			jQuery(".").html( html );

			// focus the newly added item
			var focusNum = String( count );
			document.getElementById( identifier + focusNum ).focus();
		},
		
		addUrl : function ()
		{
			// get the current count of urls
			var count = document.getElementById("url_count");
			var i = parseInt( count.value );
			
			// generate empty url datafields for one new url
			var html = "<tr>\n<td>\n<label class=\"block\" for=\"url_" + i + "\">\nURL\n</label>\n"
						+ "<input name=\"url_" + i + "\" id=\"url_" + i + "\" type=\"text\" size=\"50\" class=\"citationEditValue\" />\n</td>\n</tr>\n"
						+ "<tr>\n<td>\n<label class=\"block\" for=\"pref_" + i + "\">\nUse as title link?\n</label>\n"
						+ "<input class=\"prefLink\" name=\"pref_" + i + "\" id=\"pref_" + i + "\" type=\"checkbox\" value=\"preferred\" onclick=\"javascript:trackPreferredUrl('pref_" + i + "');\" class=\"citationEditValue\" />\n</td>\n</tr>\n"
						+ "<tr>\n<td>\n<label class=\"block\" for=\"label_" + i + "\">\nLabel\n</label>\n"
						+ "<input name=\"label_" + i + "\" id=\"label_" + i + "\" type=\"text\" size=\"20\" class=\"citationEditValue\" />\n<hr />\n</td>\n</tr>\n";
			
			// jQuery: insert new blank url datafields before the #bottomRow
			$("#bottomRow").before( html );
			
			// focus the newly added item
			document.getElementById( "url_" + i ).focus();
			
			// increment the count
			count.value = String( i + 1 );
			
			// resize the frame because we've added a new set of datafields
			//resizeFrame();
		}

	};
}();