//various utility vars

//load symbol related data
//cannot use $.getJSON directly as it is async
//we need a synchronous call to load data into the var.
var symbolsToContexts;
$.ajax({
  url: 'symbols.json',
  dataType: 'json',
  data: [],
  async: false,
  success: function(data){
     symbolsToContexts = data;
  }
});


var linesToSymbols;
$.ajax({
  url: 'lineids.json',
  dataType: 'json',
  data: [],
  async: false,
  success: function(data){
    linesToSymbols = data;
  }
});
     
var symsToKernels;
$.ajax({
  url: 'symstokernels.json',
  dataType: 'json',
  data: [],
  async: false,
  success: function(data){
    symsToKernels = data;
  }
});


function toggleGenFiles(){
  $(".gen-sourcefile-header").each(function (index, domEle){
    $(domEle).click(function(){
      $(domEle).next().toggle();
    });    
  });
}

//return a list of declSites
function getContent(lineId){
  var res ="<div id=\"symbols-accordion\" class=\"accordion-group\">";
  var content = linesToSymbols[lineId];
  if(content == null || content.length==0) return res + "no content </div>";
  for(idx in content){
    var sym = content[idx];
    var sites = symsToKernels[sym];
    if(!(typeof sites == "undefined" || sites["declSite"]==="")){
      res += 
        "<div class=\"accordion-heading\">"
        +"<a id=\"gensym-"+sites["declSite"]+"\" class=\"gensym accordion-toggle\" href=\"#collapse-"+sym+"\" data-parent=\"#symbols-accordion\" data-toggle=\"collapse\">"
        +"<b>"+sym+": </b>"+sites["declSite"]
        +" (declaration site)</a>"
        +"</div>"; //close accordion-heading

      //the collapsible list of other sites
      res+=
        "<div id=\"collapse-"+sym+"\" class=\"accordion-body collapse\" style=\"height:0px;\">"
        +"<div class=\"accordion-inner\">"
        +"<ul>";
      var otherSites = sites["otherSites"];
      for(otherIdx in otherSites){
        res +=
          "<li>"
          +"<a id=\"gensym-"+otherSites[otherIdx]+"\" class=\"gensym\" href=\"#\">"
          +"<b>"+sym+": </b>"+otherSites[otherIdx]
          +"</a>"
          +"</li>";
      }
      res+= "</ul>"
        +"</div>" //close accordion-inner
        +"</div>"; //close accordion-body 
    }
  }
  res += "</div>" //close accordion-group
    return res;
}

//display the list of syms related to a source line
function showRelatedSymbols(){
  $(".source-line").each(function(index, domEle){
      $(domEle).click(function(){
        var content = getContent($(domEle).attr("id"));
        $("#rel-symbols").html(content);
        goToByScroll("rel-symbols");
        $("#rel-symbols").effect("highlight",{color: "#ffdab9"},1000);

        //also attach a function to each of these elems so the 
        //source can be seen in the gen-source windows
        showGenSource();
      });
      $(domEle).hover(function(){
        $(domEle).toggleClass("hovered-code");
      });
  });
}

//for every clickable symbol file, show its source in the gen-source div
function showGenSource(){
  $(".gensym").each(function(index, domEle){
      $(domEle).click(function(){
        var gensymId = $(domEle).attr("id");
        var symId = gensymId.substring(7);
        var symEle = $("#"+symId).html();
        $("#gen-code").html(prettyPrintOne(symEle));
        goToByScroll("gen-code");
        $("#gen-code").effect("highlight",{color: "#ffdab9"},1000);
        showSourceContexts();
      });
   });
}

//for every symbol in gen-code tags
//show its sourcecontexts onclick
function showSourceContexts(){
  $(".code-sym").each(function(index, domEle){
    $(domEle).click(function(){
      $("#rel-scontexts").html(getSourceContexts($(domEle).text()));
      goToByScroll("rel-scontexts");
      $("#rel-scontexts").effect("highlight",{color: "#ffdab9"},1000);
      showSourceFiles();
    });      
    $(domEle).hover(function(){
        $(domEle).toggleClass("hovered-code");
    });
    $(domEle).tooltip({title: "click to show source contexts"});
  });
}

function showSourceFiles(){
  $(".source-src").each(function(index, domEle){
    $(domEle).click(function(){
      var srcFileId = $(domEle).attr("targetsrc");
      var srcHtml = $("#"+srcFileId).html();
      $("#source-code").html(prettyPrintOne(srcHtml));
      showRelatedSymbols();
    });
  });
}


//remove the prettyprint classes from source -gen divs
//add it again
//call prettyprint
/*function prettyRefresh(){
  $("#gen-code pre").addClass("prettyprint");
  $("#source-code pre").addClass("prettyprint");
prettyPrint();
}*/



//gets sourceContexts for a given symbol
function getSourceContexts(aSym){
  var res ="<div id=\"contexts-accordion\" class=\"accordion-group\">";
  
  var sContextss = symbolsToContexts[aSym];
  if(sContextss == null || sContextss.length==0) return res + "no contexts found </div>";
  //for every sourceContext, we get the top two Contexts
  for(idx in sContextss){
    res += "<div class=\"accordion-heading\">"
    +"<a id=\"sym-"+aSym+"\" class =\"accordion-toggle\" href=\"#collapse-sym-"+aSym+idx+"\" data-parent=\"#contexts-accordion\" data-toggle=\"collapse\">"
    +"<b>"+aSym+"</b> (sourceContext " + idx + ")"
    +"</a>"
    +"</div>"; //close accordion-heading

    res += "<div id=\"collapse-sym-"+aSym+idx+"\" class=\"accordion-body collapse\" style=\"height:0px;\">"
    +  "<div class=\"accordion-inner\">"
    +  "<ul>";

    var sContexts = sContextss[idx];
    for(contextidx in sContexts){
      var sContext = sContexts[contextidx];
      
      var baseName = relativePath(sContext["fileName"]);
      var noExt = noExtension(baseName);
      if(baseName.indexOf(".scala")){
        res += "<li>"
            +  "<a targetsrc=\""+noExt+"\" class=\"source-src\" href=\"#\">"
            +  "<b>"+noExt+"_"+sContext["line"]+"</b> : "+ sContext["opName"]
            +  "</a>"
            +  "</li>";
      }
    }
    res+= "</ul>"
      +"</div>" //close accordion-inner
      +"</div>"; //close accordion-body 
  }
  
  res += "</div>" //close accordion-group
  return res;
}

function relativePath(fileName){
   var i = fileName.lastIndexOf('/');
   return fileName.substring(i + 1);
}

function noExtension(baseName){
   return (baseName.indexOf(".scala") != -1) ?
     baseName.substring(0, baseName.length - 6) : baseName;
}

/* taken from http://djpate.com/2009/10/07/animated-scroll-to-anchorid-function-with-jquery/ */
function goToByScroll(id){
 	$('html,body').animate({scrollTop: $("#"+id).offset().top},'slow');
}


//loading the page
function load(){
  showRelatedSymbols();
  drawTimeline();
  //let the collapsing begin$(".collapse").collapse();
}