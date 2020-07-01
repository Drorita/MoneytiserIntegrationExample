$(document).ready(function(){

    if (window.localStorage.getItem("showCookieBar") == null)
        window.localStorage.setItem("showCookieBar", 'show');
    if (window.localStorage.getItem("showCookieBar") == "show")
        $('.cookie-disclaimer').show();
    else $('.cookie-disclaimer').hide();

    $('.cookie-disclaimer .disclaimer-close, .cookie-disclaimer .close-link').on('click', function() {
        console.log('helo...>>>')
        window.localStorage.setItem("showCookieBar", 'hide');
        $('.cookie-disclaimer').hide();
    });

    /******** Do Not Changes Variable *********/
    var pathUrl = window.location.pathname,
        winWidth = $(window).innerWidth();

    $( function() {
        $( ".newaccordion" ).accordion({
            active: false,
            collapsible: true,
            heightStyle: "content"
        });
    } );

    $('.navbar-header').on('click', function(){
        $(this).siblings().toggleClass('visible');
        $('.overlay').toggleClass('visible');
    });
    $('.overlay').on('click', function(){
        $('.navbar-header').siblings().removeClass('visible');
        $('.overlay').removeClass('visible');
    });



    $('body').append('<div class="image-overlay"><div class="close-popup"><span>&#x2A2F;</span></div><img src="" /></div>');
    var contentImage = $('.middle-sec p img, .middle-sec li img'),
        imageOverlay = $('.image-overlay'),
        overlayImage = $('.image-overlay img'),
        imageSource;
    contentImage.on('click', function () {
        imageSource = $(this).attr('src');
        overlayImage.attr('src', imageSource);
        imageOverlay.fadeIn(500);
        $('body').css('overflow', 'hidden');
    });
    $('.image-overlay, .close-popup').on('click', function () {
        imageOverlay.fadeOut(500);
        $('body').css('overflow', 'auto');
    });
    overlayImage.on('click', function (event) {
        event.stopImmediatePropagation();
    });

    // Search field click to expand
    if(winWidth > 991) {
        var inputBtn = $('.input-bx'),
            searchDiv = $('.search'),
            defaultWidth = searchDiv.css('width'),
            expandWidth = "600px";
        inputBtn.attr('autocomplete', 'off');
        inputBtn.on('focus', function () {
            searchDiv.animate({
                width: expandWidth
            });
        }).on('blur', function () {
            searchDiv.animate({
                width: defaultWidth
            });
        });
    }

    $('.mob-search').on('click', function(){
        $('.search').show();
        $('.input-bx').val('').focus();
        $(this).hide();
    });
    $('.hide-search').on('click', function(){
        $('.search').hide();
        $('.mob-search').show();
    });

/*    var searchParam = window.location.search,
        splitSearchParam = searchParam.replace('?search=', ''),
        searchParamText = splitSearchParam.replace(/(\+)+|(\s){2,}/g, ' ').replace(/(^\s+)+|(\s+$)/g, ''),
        decodeResult = decodeURIComponent(searchParamText),
        searchValue = inputBtn.val();
    if ($.trim(searchValue).length == 0) {
        inputBtn.val(decodeResult);
    }*/

    /****** Tab Menu ******/
    $(".tab-area").parent('li').addClass('tab-list');
    var tabArea = "ul.tab-area li",
        tabContent = '.tab-content';
    $(tabArea).add(tabContent).each(function () {
        $(this).siblings(':first-child').addClass('active');
    });
    $(tabArea).on('click', function(){
        $(this).each(function(){
            var tabIndex = $(this).index();
            $(this).siblings().removeClass('active');
            $(this).parent('ul').next(".tab-wrap")
                .find(tabContent).removeClass('active');
            $(this).addClass('active');
            $(this).parent('ul').next(".tab-wrap")
                .find(tabContent).eq(tabIndex).addClass('active');
        });
    });

    $('table').wrap('<div class="tbl-area"></div>');
    $('pre').wrap('<code></code>');
    $('code').prepend('<a class="copy-btn"><span></span></a>');

    /* Add class if 'Note:' text found in paragraph */
    var noteSearch = 'Note:';
    $('.middle-sec p').each(function(){
       if($(this).text().indexOf(noteSearch) > -1){
           $(this).addClass('note')
       }
    });

    // Marketplace script start
    /* Owl carousel init */
    var owl = $(".owl-carousel");
    owl.owlCarousel({
        items: 2,
        loop: false,
        nav: true,
        margin: 10,
        dots: false,
        mouseDrag: false,
        responsive: {
            0: {
                items: 1
            },
            500: {
                items: 2
            },
            1000: {
                items: 2
            }
        }
    });

    /* Lightbox init */
    lightbox.option({
        'resizeDuration': 200,
        'wrapAround': true
    });

    /* init Isotope */
    var $grid = $('.grid').isotope({
        itemSelector: '.element-item',
        layoutMode: 'fitRows',
        transitionDuration: '0',
        getSortData: {
            name: '.name',
            symbol: '.symbol',
            number: '.number parseInt',
            category: '[data-category]',
            weight: function (itemElem) {
                var weight = $(itemElem).find('.weight').text();
                return parseFloat(weight.replace(/[\(\)]/g, ''));
            }
        }
    });

    // filter functions
    var filterFns = {
        // show if number is greater than 50
        numberGreaterThan50: function () {
            var number = $(this).find('.number').text();
            return parseInt(number, 10) > 50;
        },
        // show if name ends with -ium
        ium: function () {
            var name = $(this).find('.name').text();
            return name.match(/ium$/);
        }
    };

    // bind filter button click
    $('#filters').on('click', 'a', function () {
        var filterValue = $(this).attr('data-filter');
        // use filterFn if matches value
        filterValue = filterFns[filterValue] || filterValue;
        $grid.isotope({filter: filterValue});
    });

    // change is-checked class on buttons
    $('.button-group').each(function (i, buttonGroup) {
        var $buttonGroup = $(buttonGroup);
        $buttonGroup.on('click', 'a', function () {
            $buttonGroup.find('.is-checked').removeClass('is-checked');
            $(this).addClass('is-checked');
        });
    });

    $(".content-area h2, .content-area h3").each(function(index){
        var h2Txt = $(this).text();
        if(h2Txt == 'Multiple' && index == 5){
            h2Txt = h2Txt+'-type';
        }
        applyID(this, h2Txt);
    });

    function applyParentID(elem, value){
        $(elem).attr('parent', value);
    }

    function applyID(elem, value){
        var newValue = value.toLowerCase().replace(/[^\w]/gi, '-').replace(/[-]+/g, '-').replace(/^-+|-+$/g, '');
        $(elem).attr('id', newValue );
    }

    $(".content-area h2, .content-area h3").on('click',function(e){
        window.location.hash = $(this).attr("id");
        $(window).scrollTop($(this).offset().top - 70);
    });

    /****** Right Sidebar Creation ******/
    var heading = $(".middle-sec h2"),
        subHeading = $(".middle-sec h3"),
        loopLength = heading.add(subHeading).length,
        ArrObj = [];
    heading.add(subHeading).each(function(index){
        var ulList = $('<ul/>'),
        listElement = $(this), href, title, type, anchor, list;

        href = listElement.attr('id') ? '#'+listElement.attr('id'): '#';
        title = listElement.text();

        if(listElement.is(heading)) {
            if(ArrObj.length > 0){
                ArrObj.forEach(function (item) {
                    item.appendTo(ulList);
                });
            }
            ArrObj = [];
            $("#right-sidebar li.heading:last").append(ulList);
            type = 'heading';
            anchor = $('<a/>', {text:title, href:href, class:'heading-list'});
            list = $('<li>', {'class':type}, '</li>');
            list.append(anchor).appendTo('#right-sidebar');
        } else if(listElement.is(subHeading)) {
            type = 'subheading';
            anchor = $('<a/>', {text:title, href:href, class:'subheading-list'});

            list = $('<li>', {'class':type}, '</li>');
            list.append(anchor).appendTo('#right-sidebar');
            var liObj = list.append(anchor);
            ArrObj.push(liObj);
            if(index + 1 == loopLength){
                if(ArrObj.length > 0){
                    ArrObj.forEach(function (item) {
                        item.appendTo(ulList);
                    });
                }
                ArrObj = [];
                $("#right-sidebar li.heading:last").append(ulList);
            }
        }
    });

    /* activate scrollspy menu */
    var $body   = $(document.body);
    var navHeight = $('.navbar').outerHeight(true) + 82;

    $body.scrollspy({
        target: '.scroll-col',
        offset: navHeight
    });

    /* smooth scrolling sections */
    var rightLinks = $('#right-sidebar a[href*=#]:not([href=#])');
    rightLinks.click(function() {
        if (location.pathname.replace(/^\//,'') == this.pathname.replace(/^\//,'') && location.hostname == this.hostname) {
            var target = $(this.hash);
            target = target.length ? target : $('[name=' + this.hash.slice(1) +']');
            if (target.length) {
                var hash = this.hash;
                $('html,body').animate({
                    scrollTop: target.offset().top - 80
                }, 700, function(){
                    window.location.hash = hash;
                    $(window).scrollTop(target.offset().top - 80);
                });
                return false;
            }
        }
    });

    /* Highlight js Init */
    $('code').each(function(i, block) {
        hljs.highlightBlock(block);
    });

    var internalUrl;
    $("#s-menu a").each(function(){
        internalUrl = $(this).attr('href');
        var linksLength = internalUrl.split("/");
        if(internalUrl != '') {
            if ($(this).attr('href') == pathUrl || pathUrl.indexOf(internalUrl) != -1) {
                $(this).parent().addClass('active');
            }
            else if(pathUrl == '/platforms/javascript-browser' || pathUrl == '/docs/platforms/javascript-browser'){
                $('a[href="/docs/platforms/java"]').each(function(){
                    $(this).parent().removeClass('active')
                })
            }
        }
    });


    if(pathUrl == '/tools-and-frameworks/web-framework-contentstack-express/overview'){
        //$("#s-menu a[href='/tools-and-frameworks/web-framework-contentstack-express']").parent().addClass('active');
        $("#s-menu a[href='/overview']").parent().removeClass('active');
    }
    if(pathUrl == '/docs/'){
        $('.nav-wrap .homeLink').addClass('active');
    }else{
        $('.nav-wrap .homeLink').removeClass('active');
    }
    $('#s-menu .active').each(function(){
        $(this).parents().removeClass('active')
    });

    var menuLength = $("#s-menu ul");
    $(menuLength).filter(function(){
        var $this = $(this).children("li");
        var activeList = $(this).children("li.active");
        if($this.length > 4){
          $(this).children("li.check-visibility").hide();
          $(this).append("<li class='show-more'>More..</li>");
        }
        $(this).find("li.active")
            .siblings("li").show()
            .siblings("li.active").show()
            .siblings("li.show-more").hide();
    });

    $("li.show-more").on('click', function(){
        $(this).siblings("li").show();
        $(this).hide();
    });

    var headText = "";
    $(".content-area h2, .content-area h3").each(function(){
        var heading = $(this).text().toLowerCase().replace(/[^\w]/gi, '-');
        //applyID(this, heading);
        headText = headText + "<li><a href='"+"#"+heading+"'>"+heading+"</a></li>";

    });

    $(".scroll-col").prepend("<h4>On This Page</h4>");

    /* Hide right sidebar if there will no items */
    var listLength = $("#right-sidebar li").length;
    if(listLength == 0){
        $(".scroll-col").hide();
    }


    /* Remove right sidebar from these pages */
    if(pathUrl == '/docs/tools-and-frameworks/static-site-generator' || pathUrl == '/docs/tools-and-frameworks/content-migration'){
        $(".scroll-col").remove();
    }


    /* Breadcrumb using pathname */
    var arr = pathUrl.split('/'),
        breadcrumbSec = $(".bread-crumb ul");
    var home = env==="development"? "<li><a href='/'>Home</a></li>" : "<li><a href='/docs'>Home</a></li>";
    breadcrumbSec.prepend(home);
    if(env == 'development') {
        arr.forEach(function (item, index, env) {
            if (item != '') {
                var printItem = item.replace(new RegExp('-', 'gi'), " ");
                if (arr.length - 1 !== index) {
                    if (arr[index - 1] != '') {
                        if(index==4){
                            breadcrumbSec.append("<li><a href=/"+arr[index-3]+'/'+arr[index-2]+'/'+arr[index-1]+'/'+ item + ">" + printItem + "</a></li>");
                        }else if(index==3){
                            breadcrumbSec.append("<li><a href=/"+arr[index-2]+'/'+arr[index-1]+'/'+ item + ">" + printItem + "</a></li>");
                        }
                        else {
                            breadcrumbSec.append("<li><a href=/" + arr[index - 1] + '/' + item + ">" + printItem + "</a></li>");
                        }
                    }
                    else {
                        breadcrumbSec.append("<li><a href=/" + item + ">" + printItem + "</a></li>");
                    }
                }
                else {
                    breadcrumbSec.append("<li>" + printItem + "</li>");
                }
            }
        });
    }
    else{
        arr.forEach(function(item, index, env) {
            if(item != '') {
                var printItem = item.replace(new RegExp('-', 'gi'), " ");
                if(arr.length -1 !== index) {
                    if(arr[index-1] != ''){
                        if(index==4){
                            breadcrumbSec.append("<li><a href=/"+arr[index-3]+'/'+arr[index-2]+'/'+arr[index-1]+'/'+ item + ">" + printItem + "</a></li>");
                        }else if(index==3){
                            breadcrumbSec.append("<li><a href=/"+arr[index-2]+'/'+arr[index-1]+'/'+ item + ">" + printItem + "</a></li>");
                        }
                        else{
                            breadcrumbSec.append("<li><a href=/"+arr[index-1]+'/'+ item + ">" + printItem + "</a></li>");
                        }
                    }
                    else if(index==1){
                        breadcrumbSec.append();
                    }
                    else{
                        breadcrumbSec.append("<li><a href=/"+ item + ">" + printItem + "</a></li>");
                    }
                }
                else{
                    breadcrumbSec.append("<li>" + printItem + "</li>");
                }
            }
        });
    }
    var lastSegment = arr[arr.length-1];
    if(lastSegment == 'ios'){
        $('.bread-crumb ul li:last').css("text-transform", "none").text('iOS');
    }

    /* Load more for search page */
    var loadMoreBtn = $("#loadMore"),
        hiddenResult = $(".search-result:hidden");
    hiddenResult.slice(0, 10).show();
    setTimeout(function(){
        loadMoreBtn.css('visibility','visible');
    },1000);

    if (hiddenResult.length <= 10) {
        loadMoreBtn.hide();
    }
    loadMoreBtn.on('click', function (e) {
        var hiddenResult = $(".search-result:hidden");
        e.preventDefault();
        if (hiddenResult.length < 10) {
            loadMoreBtn.hide();
        }
        hiddenResult.slice(0, 10).slideDown();
        $('html,body').animate({
            scrollTop: $(this).offset().top - 80
        }, 800);

    });

    // Scroll to top
    var scroll_ID = $("#back-top");
    $(window).scroll(function (event) {
        var scroll_top = $(window).scrollTop();
        if (scroll_top >= 250) {
            (scroll_ID).addClass("show-arr");
        } else {
            (scroll_ID).removeClass("show-arr");
        }
    });
    (scroll_ID).click(function () {
        $("html, body").animate({scrollTop: 0}, 300);
    });

    $('#right-sidebar .active').each(function(){
        $(this).parents().removeClass('active');
    });

    $('#right-sidebar').on('activate.bs.scrollspy', function () {
        $('.heading').removeClass('no-border');
        $('.active').each(function(){
            if($(this).parents('.active')){
                $(this).parents('.active').addClass('no-border');
            }
        });
    });

    var clipboardSnippets = new Clipboard('.copy-btn', {
        target: function(trigger) {
            return trigger.nextElementSibling;
        }
    });
    clipboardSnippets.on('success', function(e) {
        e.trigger.classList.add("copied");
        e.clearSelection();
        setTimeout(function () {
            e.trigger.classList.remove("copied");
        },1200);
    });
    clipboardSnippets.on('error', function(e) {
        //showTooltip(e.trigger, fallbackMessage(e.action));
    });

    // Add class when find Tutorial Video text
    var tutorialVideo = $('.middle-sec h4:contains("Tutorial Video")');
    tutorialVideo.each(function() {
        return $(this).text() == "Tutorial Video";
    }).addClass('tutorial');

    var pageTitle, pageURL, formText;
    pageTitle = $('.content-area h1').text();
    pageURL = window.location.origin + window.location.pathname;

    function feedbackErrMsg(){
        if($(".error-msg").length){
            $(".error-msg").text("An error occured. Please try again.");
        }
        else{
            $('.help-info').append("<div class='error-msg'>An error occured. Please try again.</div>")
        }
    }
    $(".btn-wrap button").on('click', function(){
       $(this).addClass('selected').siblings().removeClass('selected');
        if($(this).hasClass('no-btn')){
            $('.feedback').slideDown();
            $('.feedback textarea').focus();
        }
        else if($(this).hasClass('yes-btn')){
            $('.feedback').slideUp();
            var saveData = $.ajax({
                type: 'POST',
                url: "/docs/positive",
                data: JSON.stringify({"title":pageTitle, "url":pageURL, "positive" : true,"message":" "}),
                contentType: "application/json",
                success: function(resultData) {
                    $(".feedback, .btn-wrap, .error-msg").hide();
                    $('.help-info h5').text('Thank you for your feedback!');
                }
            });
            saveData.error(function() {
                feedbackErrMsg();
            });
        }
    });
    $(".feedback input").on('click', function(e){
        formText = $('.feedback textarea').val();
        if(formText != '') {
            var saveData = $.ajax({
                type: 'POST',
                url: "/docs/negative",
                data: JSON.stringify({"title": pageTitle, "url": pageURL, "positive": false, "message": formText}),
                contentType: "application/json",
                success: function (resultData) {
                    $(".feedback, .btn-wrap, .error-msg").hide();
                    $('.help-info h5').text('Thank you for your feedback!');
                    //console.log('resultData -> ', resultData)
                }
            });
            saveData.error(function (error) {
                //console.log("Something went wrong.", error);
                feedbackErrMsg();
            });
        }
        else{
            $('.feedback textarea').addClass('error');
        }
        e.preventDefault();
    });
    $('.feedback textarea').on('keyup', function(){
        formText = $('.feedback textarea').val();
        if(formText != '') {
            $(this).removeClass('error');
        }
        else{
            $(this).addClass('error');
        }
    });

    if(winWidth > 991) {
        $(window).scroll(function () {
            var windowHeight = window.innerHeight,
                windowTopHeight = $(window).scrollTop(),
                totalHeight = windowTopHeight + windowHeight,
                footerOffset = $('footer').offset().top,
                sMenu = $('#s-menu');
                scrollRightNav = $('.scroll-col');
                gotoTop = $('.go-top');
            var scroll_top = $(window).scrollTop();
            var currentWinHeight = footerOffset - totalHeight;
            if (currentWinHeight <= 0) {
                sMenu.css('top', currentWinHeight);
                scrollRightNav.css({'top':currentWinHeight+150, 'height': 'calc(100vh - 390px)', 'margin-top':
                 '60px'});
                gotoTop.css({'bottom': -(currentWinHeight-30)});
            }
            else {
                sMenu.css('top', '0px');
                scrollRightNav.css({'top':'50px', 'height': 'calc(100vh - 190px)', 'margin-top': '108px'});
                gotoTop.css({'bottom': '29px'});
                gotoTop.css({'bottom': '70px'});
                if(scroll_top == 0){
                    gotoTop.css({'bottom': '-45px'});
                }else{
                    gotoTop.css({'bottom': '70px'});
                }
            }
        });
    }

    var iframeCount = $('iframe');
    iframeCount.each(function (index) {
        $(this).attr('id', 'player-'+index);
    });

    var platformsSelector = ['platforms', 'tools-and-frameworks', 'knowledgebase'];
    platformsSelector.forEach(function(elem, index){
        if(pathUrl.indexOf(elem) > -1){
            $("#right-sidebar > li:first-child").addClass('open-list');
        }
        else if(pathUrl.indexOf('example-apps/') > -1){
            $("#right-sidebar > li:nth-child(2)").addClass('open-list');
        }
    });

    $('.y-video').prepend('<span class="thumb play-icon"></span>');

});

/* Scroll hash link when open in new tab */
$(window).on('load', function(){
    var hashLink = window.location.hash;
    $(".middle-sec h2, .middle-sec h3, .middle-sec h4").each(function(){
        if(hashLink.indexOf($(this).attr("id")) == 1){
            $('html,body').animate({
                scrollTop: $(this).offset().top - 80
            }, 700);
        }
    });

    $(".api-change-log-detail table").each(function(){
        var newVersion = $(this).find(".new-version").height();
        var oldVersion = $(this).find(".old-version").height();
        var maxHeight = newVersion > oldVersion ? newVersion : oldVersion;
        $(this).find(".new-version").height(maxHeight);
        $(this).find(".old-version").height(maxHeight);
    });

    // add extensions class
    var pathUrl = window.location.pathname;
    var filename = pathUrl.substring(pathUrl.lastIndexOf('/'));
    if(filename === '/extensions' ){
        $('.content-area .middle-sec').addClass('extensions');
    }

});


/**************** YouTube Player Using API *********************/
function getFrameID(id) {
    var elem = document.getElementById(id);
    if (elem) {
        if (/^iframe$/i.test(elem.tagName)) return id; //Frame, OK
        // else: Look for frame
        var elems = elem.getElementsByTagName("iframe");
        if (!elems.length) return null; //No iframe found, FAILURE
        for (var i = 0; i < elems.length; i++) {
            if (/^https?:\/\/(?:www\.)?youtube(?:-nocookie)?\.com(\/|$)/i.test(elems[i].src)) break;
        }
        elem = elems[i]; //The only, or the best iFrame
        if (elem.id) return elem.id; //Existing ID, return it
        // else: Create a new ID
        do { //Keep postfixing `-frame` until the ID is unique
            id += "-frame";
        } while (document.getElementById(id));
        elem.id = id;
        return id;
    }
    // If no element, return null.
    return null;
}

// Define YT_ready function.
var YT_ready = (function() {
    var onReady_funcs = [],
        api_isReady = false;
    /* @param func function     Function to execute on ready
     * @param func Boolean      If true, all qeued functions are executed
     * @param b_before Boolean  If true, the func will added to the first
     position in the queue*/
    return function(func, b_before) {
        if (func === true) {
            api_isReady = true;
            for (var i = 0; i < onReady_funcs.length; i++) {
                // Removes the first func from the array, and execute func
                onReady_funcs.shift()();
            }
        }
        else if (typeof func == "function") {
            if (api_isReady) func();
            else onReady_funcs[b_before ? "unshift" : "push"](func);
        }
    }
})();
// This function will be called when the API is fully loaded

function onYouTubePlayerAPIReady() {
    YT_ready(true)
}

function onPlayerStateChange(event) {
    if (event.data == YT.PlayerState.ENDED) {
        var vId = event.target;
        $(vId.a).siblings().show();
    }
}
var players = {};
//Define a player storage object, to enable later function calls,
//  without having to create a new class instance again.
YT_ready(function() {
    $(".thumb + iframe[id]").each(function() {
        var identifier = this.id;
        var frameID = getFrameID(identifier);
        $(this).attr('allowFullscreen', 'true');
        if (frameID) { //If the frame exists
            players[frameID] = new YT.Player(frameID, {
                events: {
                    "onReady": createYTEvent(frameID, identifier),
                    'onStateChange': onPlayerStateChange
                }
            });
        }
    });
    });

// Returns a function to enable multiple events
function createYTEvent(frameID, identifier) {
    return function (event) {
        var player = players[frameID]; // player object
        var the_div = $('#'+identifier).parent();
        the_div.children('.thumb').click(function() {
            var $this = $(this);
            $this.fadeOut().next().addClass('play');
            setTimeout(function(){
                $this.siblings('.thumb').hide();
            },150);
            if ($this.next().hasClass('play')) {
                player.playVideo();
                //player.destroy();
            }
        });
    }
}


// Load YouTube Frame API
(function(){ //Closure, to not leak to the scope
    var s = document.createElement("script");
    s.src = "https://www.youtube.com/player_api"; /* Load Player API*/
    var before = document.getElementsByTagName("script")[0];
    before.parentNode.insertBefore(s, before);
})();
