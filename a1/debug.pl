#!/bin/perl
use CGI qw/:standard/;
$q = new CGI();

print $q->header, 
     $q->start_html(	-title => 'Debugging Help',
        	            -style => {-src=>'style.css'}),
	$q->h1('Environment Variables'),
	"QUERY_STRING: ", $ENV{'QUERY_STRING'}, $q->br, 
	"REQUEST_METHOD: ", $ENV{'REQUEST_METHOD'}, $q->br,
	"CONTENT_TYPE: ", $ENV{'CONTENT_TYPE'}, $q->br,
	"CONTENT_LENGTH: ", $ENV{'CONTENT_LENGTH'}, $q->br;

print $q->h1('Get Something..'),
	$q->startform(-action=>'debug.pl', -method=>'GET', -class=>'todo-form'),
	$q->textfield(-name=>'foo', -class=>'description'),
	$q->submit(-name=>'button', -value=>'Submit', -class=>'submitbutton'),
	$q->end_form();
  
print $q->h1('Post Something..'),
	$q->startform(-action=>'debug.pl',-class=>'todo-form'),
	$q->textfield(-name=>'bar', -class=>'description'),
	$q->submit(-name=>'button', -value=>'Submit', -class=>'submitbutton'),
	$q->end_form();

print	
	$q->start_multipart_form(-action=>'debug.pl',-class=>'todo-form'),
	$q->textfield(-name=>'baz', -class=>'description'),
	$q->submit(-name=>'button', -value=>'Submit', -class=>'submitbutton'),
	$q->end_form();
	

