#/usr/bin/perl
use CGI::Pretty qw(:standard);
$cwd = $ENV{'HOME'} . "/a1";
chdir $cwd;

$q = new CGI();

# mkdir ("$cwd/data", 0644) unless (-d "$cwd/data") || die ("unable to create dir $cwd/data: $!");

sub trim($)
{
    my $string = shift;
    $string =~ s/^\s+//;
    $string =~ s/\s+$//;
    return $string;
}

sub header {

    print $q->header,
        $q->start_html( -title => 'To-do List',
                        -style => {-src=>'style.css'}),
        $q->h1('To-Do List');
}

sub list {
    
    if (not -e "TODO") 
    {
        open(FILE, "> TODO") || die("cannot create TODO");
    }

    open(TODO, "TODO") || die("Could not open TODO!");
        
    while (<TODO>)
    {
        @words = split(/\s+/, $_); 
        my $id = $words[0];
        $q->param('id', $id);
        shift(@words);
        my $description = join(" ", @words);
        open(NOTES, "data/$id/NOTES");
        my @notes = <NOTES>;

        print $q->div ( { -class => 'todo-list' }, 
            $q->startform( -action => 'todo.pl', -method => 'GET' ),
            $q->hidden( -name => 'id', -default => $id ), 
            $q->image_button( -src => 'del.gif', -name => 'action', -value => 'delete' ), 
            $q->image_button( -src => 'edit.gif', -name => 'action', -value => 'edit' ), 
            $q->span( {-class => "description"}, $description ),
            $q->span( {-class => "notes" }, join(" ", @notes) . "&nbsp;"),
            $q->endform(), "\n"
            );
    }
    $q->param('description', "");
    print $q->startform( -action => 'todo.pl' ),
        $q->div ( { -class => 'todo-form' },
        "New To-Do Item: ",
        $q->textfield ( -name=>'description', -class=>'description'),
        $q->submit( -name=>'action', -value=>'add', -class=>'submitbutton'),
        ),
        $q->endform();
        
}

sub edit {

    open(TODO, "TODO") || die("Could not open TODO!");
    
    close(f);
    
    while (<TODO>)
    {
        @words = split(/\s+/, $_); 
        $id = $words[0];
        if ($id == $q->param('id')) 
        {
            shift(@words);
            $description = join(" ", @words);
            open(NOTES, "data/$id/NOTES");
            @notes = <NOTES>;
        }
    }
        
    print $q->start_multipart_form ( -action => "todo.pl" ),
    $q->div ( {-class=>'todo-form'},
        "Item: ", 
        $q->br,
        $q->textfield(-class=>'edittodo', -value=>"$description", -size=>51, -name=>"description" ),
        ),
        
    $q->div ( {-class=>'todo-form'},
        "Notes: ", 
        $q->br,
        $q->textarea( { -cols=>50, -rows=>5, -name=>"notes", -default => join(" ", @notes) }),
        ),

    $q->hidden( -name => 'id', -value => $id ),
    $q->submit( -name=>'action', -value=>'update', -class=>'submitbutton'),
    $q->endform();
}

sub end {
    print $q->end_html();
}

sub add {
    if ($q->param('description') eq "") 
    {
        $msg = "cannot add an empty to-do item";
    }
    else
    {
        if (not -e "TODO") {
              open(FILE, "> TODO") or die "cannot create TODO";
        }
        
        open (TODO, "TODO") || die ("cannot open TODO");
        while (<TODO>) 
        {
            if (eof(TODO))
            {
                @words = split(/\s+/, $_); 
                $id = $words[0];
            }
        }
        if ($id eq '') 
        {
            $id = 0;
        }
        $id++;
        close(TODO);
        open (TODO, ">>TODO") || die ("cannot open TODO");
        print TODO $id, " ", $q->param('description'), "\n";
        close (TODO);
        
        mkdir ("$cwd/data") unless (-d "$cwd/data") || die ("cannot create data subdirectory. $!");
        mkdir ("$cwd/data/$id") unless (-d "$cwd/data/$id");
        open(F, ">data/$id/NOTES") || die ("unable to open NOTES for writing. $!");
        close(F);
 
        $msg = "New item \"" . $q->param('description') . "\" added.";
    }
    print $q->div( { -class => 'message' }, $msg ) , "\n";

    &list;
}

sub update {
    $msg = "";
    $tmpfile = "TODO" . $$;
    open (TODO, "< TODO");
    open (NEW, "> $tmpfile");
    while (<TODO>) 
    {
        @words = split(/\s+/, $_); 
        $id = $words[0];        
        if ($id ne $q->param('id'))
        {
            print NEW $_;
        }
        else 
        {
            print NEW $id, " ", $q->param('description'), "\n";
            $msg = "Item \"" . $q->param('description') . "\" updated.";
        }
    }    
    if ($msg eq "")  
    {
        $msg = "Attempting to update a non-existence item " . $q->param('id');
        close(TODO);
        close(NEW);
        unlink($tmpfile);
    }
    else
    {
        close(TODO);
        close(NEW);
        rename("TODO", "TODO.bak");
        rename($tmpfile, "TODO");
        $notesfile = "data/".$q->param('id')."/NOTES";
        open(NOTES, ">$notesfile");
        print NOTES $q->param('notes');
        close(NOTES);        
      } 
      
    print $q->div( { -class => 'message' }, $msg ) , "\n";
    &list;
}

sub del {
    $msg = "";
    $tmpfile = "TODO" . $$;
    open (TODO, "< TODO");
    open (NEW, "> $tmpfile");
    while (<TODO>) 
    {
        @words = split(/\s+/, $_); 
        $id = $words[0];        
        if ($id ne $q->param('id'))
        {
            print NEW $_;
        }
        else 
        {
            shift @words;
            $description = join(" ", @words);
            $msg = "Item \"" . $description . "\" deleted.";
    
        }
    }
    $id = $q->param('id');
    if ($msg eq "")  
    {
        $msg = "Attempting to delete an non-existence item " . $q->param('id');
        close(TODO);
        close(NEW);
        unlink($tmpfile);
    }
    else
    {
        close(TODO);
        close(NEW);
        rename("TODO", "TODO.bak");
        rename($tmpfile, "TODO");
      
        unlink($cwd."/data/".$id."/NOTES")  || die ("Unable to delete $cwd/data/$id/NOTES. $!");
        rmdir("$cwd/data/$id") || die ("Unable to rmdir for $id. $!");
    }
    
    print $q->div( { -class => 'message' }, $msg ) , "\n";

    &list;
}

sub error {
    $msg = "Invalid action attempted: " . $q->param('action');
    print $q->div( { -class => 'message' }, $msg ) , "\n";

    &list;
}

&header;
if ($q->cgi_error())
{
    $msg = "CGI error: " . $q->cgi_error();
    print $q->div( { -class => 'message' }, $msg ) , "\n";

    &list;
    &end;
    exit;
}
if ($q->param('action') eq '') 
{
    &list;
}
elsif ($q->param('action') eq 'add')
{
    &add;
}
elsif ($q->param('action') eq 'delete')
{
    &del;
}
elsif ($q->param('action') eq 'edit') 
{
    &edit;
} 
elsif ($q->param('action') eq 'update')
{
    &update;
}
else 
{
    &error;
}
&end;
