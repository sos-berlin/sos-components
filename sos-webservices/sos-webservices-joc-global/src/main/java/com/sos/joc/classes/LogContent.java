package com.sos.joc.classes;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

import org.apache.commons.lang3.StringEscapeUtils;

public class LogContent {
    
    private static final String SPAN_LINE = "<div class=\"line %1$s\">%2$s</div>%n";
    private static final String HTML_START = "<!DOCTYPE html>%n<html>%n"
            + "<head>%n"
            + "  <title>JobScheduler - %1$s</title>%n"
            + "  <meta http-equiv=\"Content-Type\" content=\"text/html; charset=UTF-8\"/>%n"
            + "  <meta name=\"viewport\" content=\"width=device-width, initial-scale=1\"/>%n"
            + "  <style type=\"text/css\">%n"
            + "    div.log                         {font-family:\"Lucida Console\",monospace;font-size:12px;line-height:1.1em;margin-left:2px;padding-left:0}%n"
            + "    div.line                        {padding-left:3em;text-indent:-3em;}%n"
            + "    .log_error                      {color:red;}%n"
            + "    .log_warn                       {color:tomato;}%n"
            + "    .log_info                       {color:black;}%n"
            + "    .log_stderr                     {color:red;}%n"
            + "    .log_stdout                     {color:#333;}%n"
            + "    .log_debug                      {color:darkgreen;}%n"
            + "  </style>%n"
            + "</head>%n"
            + "<body class=\"log\">%n"
            /* only for rolling logs senseful */
//            + "  <script type=\"text/javascript\" language=\"javascript\">%n"
//            + "    var timer;%n"
//            + "    var program_is_scrolling    = true;%n"
//            + "    var error                   = false;%n"
//            + "    %n"
//            + "    start_timer();%n"
//            + "    %n"
//            + "    window.onscroll             = window__onscroll;%n"
//            + "    document.onmousewheel       = window__onscroll; /* IE, Chrome */%n"
//            + "    %n"
//            + "    if(document.addEventListener) { /* Safari, Firefox */%n"
//            + "        document.addEventListener('DOMMouseScroll', window__onscroll, false);%n"
//            + "    }%n"
//            + "    %n"
//            + "    if( window.navigator.appName == 'Netscape' ) {%n"
//            + "        window.onkeydown        = stop_timer;%n"
//            + "        window.onmousedown      = stop_timer;%n"
//            + "        window.onkeyup          = window__onscroll;%n"
//            + "        window.onmouseup        = window__onscroll;%n"
//            + "    }%n"
//            + "    %n"
//            + "    function start_timer() {%n"
//            + "        stop_timer();%n"
//            + "        if( !error )  timer = window.setInterval( \"scroll_down()\", 200 );%n"
//            + "    }%n"
//            + "    %n"
//            + "    function stop_timer() {%n"
//            + "        if( timer != undefined ) {%n"
//            + "            window.clearInterval( timer );%n"
//            + "            timer = undefined;%n"
//            + "        }%n"
//            + "    }%n"
//            + "    %n"
//            + "    function scroll_down() {%n"
//            + "        try {%n"
//            + "            program_is_scrolling = true;%n"
//            + "            window.scrollTo( document.body.scrollLeft, document.body.scrollHeight );%n"
//            + "        } catch( x ) {%n"
//            + "            error = true;%n"
//            + "            window.clearInterval( timer );%n"
//            + "            timer = undefined;%n"
//            + "        }%n"
//            + "    }%n"
//            + "    %n"
//            + "    function window__onscroll() {%n"
//            + "        if( !program_is_scrolling ) {%n"
//            + "            if( document.body.scrollTop + document.body.clientHeight == document.body.scrollHeight ) {%n"
//            + "                if( timer == undefined )  start_timer();%n"
//            + "            } else {%n"
//            + "                stop_timer();%n"
//            + "            }%n"
//            + "        }%n"
//            + "        program_is_scrolling = false;%n"
//            + "    }%n"
//            + "    %n"
//            + "  </script>%n"
            + "  <div class=\"log\">%n";
    private static final String HTML_END = "%n</div>%n</body>%n</html>%n";
    private Pattern stylePattern = Pattern.compile("\\]\\s*\\[(INFO|ERROR|WARN|DEBUG|STDOUT|STDERR)\\s*\\]");

    private String addStyle(String line) {
        String css = "";
        Matcher m = stylePattern.matcher(line);
        if (m.find()) {
            css = "log_" + m.group(1).toLowerCase();
        }
        return String.format(SPAN_LINE, css, StringEscapeUtils.escapeHtml4(line));
    }

    private Path pathOfColouredGzipLog(Path path, String title) throws IOException {
        BufferedReader br = new BufferedReader(new InputStreamReader(new GZIPInputStream(Files.newInputStream(path))));
        Path targetPath = path.getParent().resolve(path.getFileName().toString()+".log");
        OutputStream out = new GZIPOutputStream(Files.newOutputStream(targetPath));
        if (title != null) {
            out.write(String.format(HTML_START, title).getBytes());
        }
        String thisLine;
        while ((thisLine = br.readLine()) != null) {
            out.write(addStyle(thisLine).getBytes());
        }
        if (title != null) {
            out.write(String.format(HTML_END).getBytes());
        }
        br.close();
        out.close();
        Files.deleteIfExists(path);
        return targetPath;
    }
    
    public Path pathOfHtmlWithColouredGzipLogContent(Path log) throws IOException {
        if (log == null) {
            return null;
        }
        return pathOfColouredGzipLog(log, null);
    }
    
    public Path pathOfHtmlPageWithColouredGzipLogContent(Path log, String title) throws IOException {
        if (log == null) {
            return null;
        }
        return pathOfColouredGzipLog(log, title);
    }
}



