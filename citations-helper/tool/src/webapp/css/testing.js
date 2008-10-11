jQuery(document).ready(function() {
			testing.attachHandlers();
		}
	);

var testing = function() {
	var counter = 1;
	var hintsBlocked = false;
	
	function blockHints()
	{
		hintsBlocked = true;
	}
	function unblockHints()
	{
		hintsBlocked = false;
	}
	return {
		attachHandlers : function() {
			jQuery(".hintable").click( function() {
					testing.insertWumpaAfter(this);
				}
			);
			jQuery(".wumpa").mouseover( function() {
					blockHints();
				}
			);
			jQuery(".wumpa").mouseout( function() {
					unblockHints();
				}
			);
		},
		insertWumpaAfter : function (domElement) {
			if(!hintsBlocked)
			{
				var wumpa = "<div class=\"hintable draggable\">\n<div class=\"wumpa\">\n<h3>New Div " + counter++ + "</h3>\n</div>\n</div>\n";
				jQuery(domElement).after(wumpa);
				jQuery(document).ready(function() {
						jQuery(domElement).next(".hintable").click( function() {
								testing.insertWumpaAfter(this);
							}
						);
						jQuery(domElement).next(".hintable").children(".wumpa").mouseover( function() {
								blockHints();
							}
						);
						jQuery(domElement).next(".hintable").children(".wumpa").mouseout( function() {
								unblockHints();
							}
						);
					}
				);
			}
		}
		

	};
}();
