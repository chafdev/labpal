$(document).ready(function()
{
	//slect all
	$("table.exp-table .top-checkbox").click(function() {
		$(this).closest("table.exp-table").find(".side-checkbox").not(this).prop("checked", this.checked);
	});

	//filter by enumeration or/and interval
	$("#experiment-filter .btn-filter").click(filter_experiments); 

	$("table.exp-table").tablesorter();
	$(".pulldown-contents").hide();
	$(".pulldown").addClass("closed");
	$(".pulldown").click(function() {
		$(this).toggleClass("closed");
		$(this).closest("div.around-pulldown").find(".pulldown-contents").toggle();
	});

	$("pre.multiline").click(function() {
		$(this).toggleClass("open");
	});   
	resize_top_menu();
});

/**
 * Filters the list of experiments in a page based on a text
 * string specifiying range
 * @returns Nothing
 */
function filter_experiments() 
{
	var hideLigne = $(".top-filter").val().trim();
	if (hideLigne =="")
	{
		$("table.exp-table .tr").show();
		$("table.exp-table .side-checkbox").prop("disabled", false);
	}
	else
	{
		$("table.exp-table .tr").hide();
		$("table.exp-table .side-checkbox").prop("disabled", true);
		hideLigne = hideLigne.replace(/[^0-9,\-]/g, "");
		console.log(hideLigne);
		var hideLigneArray = hideLigne.split(",");
		for (var i = 0; i < hideLigneArray.length; i++)
		{
			if (hideLigneArray[i]!='') {//inputs like ',9,'
				if(hideLigneArray[i].indexOf('-')>-1)
				{
					var hideLigneArray2 = hideLigneArray[i].split("-");
					if (hideLigneArray2[0]!='' && hideLigneArray2[1]!='') 
					{//inputs like '-5'
						for (var k = parseInt(hideLigneArray2[0]); k <= parseInt(hideLigneArray2[1]); k++)
						{	      
							$("table.exp-table .tr-"+k).show();
							$("table.exp-table .side-checkbox-"+k).prop("disabled", false);
						}
					}
				}
				else
				{
					$("table.exp-table .tr-"+hideLigneArray[i]).show();
					$("table.exp-table .side-checkbox-"+hideLigneArray[i]).prop("disabled", false);
				}
			}
		}
	}
};


/**
 * Toggle the captions on the top menu
 * depending on the width of the viewport
 */
function resize_top_menu() {
	if ($(window).width() < 1175) {
		$("#top-menu li").addClass("small-screen");
	}
	else {
		$("#top-menu li").removeClass("small-screen");
	}
};

/**
 * Resize menu on window resize
 */
$(window).resize(resize_top_menu);
