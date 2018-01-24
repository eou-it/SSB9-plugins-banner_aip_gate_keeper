/*******************************************************************************
 Copyright 2015-2018 Ellucian Company L.P. and its affiliates.
 *******************************************************************************/
import java.text.ParseException
import java.text.SimpleDateFormat


includeTargets << grailsScript("_GrailsInit")

/**
 * Find all copyright errors in files modified in app and plugins with branch name same as app
 *
 */

target(checkCopyrightXe: "Find incorrect copyright years. Use \"-fix\" to correct files with incorrect copyright years.") {
    if(argsMap.containsKey('fix')) {
        println " Modifying files with incorrect copyright years"
        doCheckCopyrightXe(true)
    }
    else{
        doCheckCopyrightXe(false)
    }
}

def doCheckCopyrightXe(fixIt){
    def ln = File.separator == '\\' ? '\\\\' : File.separator
    def appDirectoryName = new File(System.properties['base.dir'])
    def appDirectoryName_txt = appDirectoryName.toString()
    def output = new File(appDirectoryName_txt + ln + "target" + ln + "copyrighterrors-report.html")
    // get current branch
    def command2 = "git rev-parse --abbrev-ref HEAD"
    def proc2 = command2.execute(null, appDirectoryName)
    proc2.waitFor()
    if (proc2.exitValue() != 0) {
        println "Error, ${proc.err.text}"
        System.exit(-1)
    }
    def branches = proc2.in.text.readLines().collect {
        it.replaceAll(/[a-z0-9]*\trefs\/heads\//, '')
    }
    def branch = branches[0]

    def appPlugins = []
    appPlugins.add(appDirectoryName)
    // find plugins that are on same branch
    //def plugins = findPlugins(appDirectoryName, ln, branch)
    //appPlugins.addAll(plugins)
    //
    def year = new Date().format("YYYY")

    def numberErrors = 0
    def numberFiles = 0
    def copyrightBody = ""
    def correction = fixIt ? '<th>Correction</th>' : ''

    appPlugins.each {
        def directoryname = it
        def command = "git diff --name-only origin/master"
        def proc = command.execute(null, directoryname)
        StringBuffer out = new StringBuffer()
        StringBuffer err = new StringBuffer()
        proc.waitForProcessOutput(out, err)
        if (proc.exitValue() != 0) {
            println "Error, ${err.toString()}"
            System.exit(-1)
        }

        changes = out.toString().readLines().collect {
            it.replaceAll(/[a-z0-9]*\trefs\/heads\//, '')
        }

        def excludedFileExts = ["rst", "properties", "svg", "scss", "ttf", "woff", "woff2", "eot", "png", "gif", "map",
                                "jar"]
        def excludedFiles = "CheckCopyrightXe.groovy|.git|xe-ui-components|ckeditor|d3.min.js"

        changes.unique().each {
            def filename_dir_txt = directoryname.toString() + ln + it
            def filename = new File(filename_dir_txt)
            def filename_txt = it
            def fileNameExt = filename.name.tokenize('.').last()

            if (!(filename_txt =~ excludedFiles || excludedFileExts.contains(fileNameExt))) {
                if (!filename.isDirectory() && filename.exists()) {
                    def fileIoStream = new RandomAccessFile(filename_dir_txt, "rw")
                    File tempFile
                    def tempStream
                    if(fixIt) {
                        tempFile = new File("tmpCCXE_" + System.currentTimeMillis())
                        tempStream = new RandomAccessFile(tempFile, "rw")
                    }

                    def commitDetail = findCommitDetail(filename_txt, directoryname)
                    def yearChanged = getYearChanged(commitDetail)
                    if (!yearChanged) yearChanged = year

                    numberFiles += 1
                    def lineNo = 1
                    def noCopyRightLine = true
                    def firstXmlLine = ''
                    String EOL = '\r\n'
                    try {
                        use(RandomAccessFileEach) {
                            fileIoStream.eachLine { line ->
                                if(!firstXmlLine && fileNameExt == 'xml' && line.toString().toUpperCase() =~ /<\?XML/) {
                                    firstXmlLine = line + '\r\n';
                                }
                                if(filename_txt =~ 'test/integration/net/hedtech/banner/DateUtilityIntegrationTests.groovy')
                                    lineNo++
                                else
                                    ++lineNo
                                if (line.toString().toUpperCase() =~ "COPYRIGHT") {
                                    noCopyRightLine = false
                                    if (!(line =~ yearChanged)) {
                                        if(fixIt) {
                                            fileIoStream.seek(fileIoStream.getFilePointer()-2)
                                            char testChar1 = fileIoStream.read() as char
                                            char testChar2 = fileIoStream.read() as char
                                            char CR = '\r', LF = '\n'
                                            if(testChar2 == LF){
                                                if(testChar1 == CR) {
                                                    // EOL is CRLF
                                                    EOL = ''+CR+LF
                                                }
                                                else {
                                                    // EOL is LF
                                                    EOL = LF
                                                }
                                            }
                                            else if(testChar2 == CR){
                                                // EOL is CR
                                                EOL = CR
                                            }

                                            def correctedLine = getCorrectedCopyright(line, yearChanged, EOL)
                                            def numBytes = line.size()
                                            def writeOffset = fileIoStream.getFilePointer() - numBytes - EOL.size()
                                            def toEOF = fileIoStream.length() - fileIoStream.getFilePointer()
                                            def newFileLength = fileIoStream.length() - numBytes + correctedLine.size()

                                            fileIoStream.getChannel().transferTo(0 as int, writeOffset as int, tempStream.getChannel())
                                            tempStream.writeBytes correctedLine
                                            fileIoStream.getChannel().transferTo(fileIoStream.getFilePointer(), toEOF, tempStream.getChannel())
                                            tempStream.seek 0
                                            fileIoStream.getChannel().transferFrom(tempStream.getChannel(), 0, newFileLength)

                                            copyrightBody += "<tr><td>${directoryname.toString()}</td><td>${filename_txt}</td><td>${line}</td><td>${correctedLine}</td><td>${commitDetail.toString()}</td></tr>"
                                        }
                                        else {
                                            copyrightBody += "<tr><td>${directoryname.toString()}</td><td>${filename_txt}</td><td>${line}</td><td>${commitDetail.toString()}</td></tr>"
                                        }
                                        numberErrors += 1
                                    }
                                }
                                noCopyRightLine
                            }

                            if (noCopyRightLine) {
                                def noCopyRightLineText = "No copyright statement identified in file"

                                if(fixIt) {
                                    try {
                                        def correctedLine = firstXmlLine + getNewCopyrightText(fileNameExt, yearChanged, EOL)

                                        tempStream.writeBytes correctedLine
                                        fileIoStream.getChannel().transferTo(firstXmlLine.size(), fileIoStream.length(), tempStream.getChannel())
                                        tempStream.seek 0
                                        fileIoStream.getChannel().transferFrom(tempStream.getChannel(), 0, tempStream.length())

                                        copyrightBody += "<tr><td>${directoryname.toString()}</td><td>${filename_txt}</td><td>${noCopyRightLineText}</td><td>${groovy.xml.XmlUtil.escapeXml(correctedLine)}</td><td>${commitDetail.toString()}</td></tr>"
                                    }
                                    catch (RuntimeException ex) {
                                        copyrightBody += "<tr><td>${directoryname.toString()}</td><td>${filename_txt}</td><td>${noCopyRightLineText}</td><td>File extension was not recognized<td>${commitDetail.toString()}</td></tr>"
                                    }
                                }
                                else {
                                    copyrightBody += "<tr><td>${directoryname.toString()}</td><td>${filename_txt}</td><td>${noCopyRightLineText}</td><td>${commitDetail.toString()}</td></tr>"
                                }
                                numberErrors += 1
                            }
                        }
                    }
                    finally {
                        fileIoStream.close()
                        if(fixIt) {
                            tempStream.close()
                            tempFile.delete()
                        }
                    }
                }
            }
        }
    }
    output.write """
	    <html>
	        <body>
	            <head>
	                <title>Copyright Error Report for files in branch ${branch}</title>
	                <style type="text/css">
	                    table.report {
	                        border-width: 1px;
	                        border-spacing: 1px;
	                        border-style: outset;
	                        border-color: gray;
	                        border-collapse: separate;
	                        background-color: white;
	                    }
	                    table.report th {
	                        border-width: 1px;
	                        padding: 2px;
	                        border-style: inset;
	                        border-color: gray;
	                        background-color: lightblue;
	                        -moz-border-radius: 0px 0px 0px 0px;
	                        white-space: nowrap;
	                    }
	                    table.report td {
	                        border-width: 1px;
	                        padding: 2px;
	                        border-style: inset;
	                        border-color: gray;
	                        background-color: white;
	                        -moz-border-radius: 0px 0px 0px 0px;
	                    }
	                </style>
	            </head>
	            <h2>Copyright Error Report for files modified in App ${appDirectoryName_txt} branch ${branch}</h2>
	            Generated: ${new Date()}
	            <table class="report">
	                <thead>
                        <th>Path</th>
	                    <th>File</th>
	                    <th>Copyright Statement</th>
                        ${correction}
                        <th>Commit Details</th>
	                </thead>
	                <tfoot>
	                    <tr>
	                        <td colspan="4">Total: ${numberErrors} Errors</td>
	                    </tr>
	                </tfoot>
	                <tbody>
	                    ${copyrightBody}
	                </tbody>
	            </table>
	        </body>
	    </html>"""


    println "Number unique files revised  in branches ${branch} is ${numberFiles}, number with incorrect copyright ${numberErrors}"
    println "Review report  ${output.toString()}"
    if (numberErrors > 0) {
        System.exit(-1)
    }
}

// target for process, execute as:   grails checkCopyrightXe

setDefaultTarget(checkCopyrightXe)

// helper methods to get git log and parse date changed
def gitLog(def filename_txt, def directoryname) {
    ['git', 'log', '-n', '1', "${filename_txt}"].execute(null, directoryname).text.trim()
}


def findCommitDetail(def filename_txt, def directoryname) {

    def procc = gitLog(filename_txt, directoryname)

    def commitinfo = procc?.readLines()?.collect {
        it.replaceAll(/[a-z0-9]*\trefs\/heads\//, '')
    }

    return commitinfo
}


def getYearChanged(commitDetail) {
    def valueDate
    def date = ""
    commitDetail.each {
        if (it =~ "Date:") {
            date = it.replace("Date:", "")
        }
    }
    valueDate = date.trim()

    def valueYear = parseYearFromVCLog(valueDate)
    return valueYear
}


def parseYearFromVCLog(date) {
    SimpleDateFormat sdfY = new SimpleDateFormat('yyyy')
    def dateFormats = ["yyyyMMdd",
                       "MMddyyyy",
                       "yyyy-MM-dd",
                       "MM/dd/yyyy",
                       "dd/MM/yyyy", 'EEE, d MMM yyyy HH:mm:ss Z',
                       'EEE MMM d yyyy HH:mm:ss Z',
                       'EEE MMM d HH:mm:ss yyyy Z'
    ]

    def year
    dateFormats.each {
        SimpleDateFormat sdf = new SimpleDateFormat(it)
        try {
            def testdate = sdf.parse(date)
            sdf.applyPattern("yyyy")
            def yearTest = sdf.format(testdate)
            if (yearTest >= "2010" && yearTest <= "2030") {
                year = yearTest
                return true
            }
        }
        catch (ParseException e) {
        }
    }
    return year
}


def getCorrectedCopyright(String line, year, EOL){
    def firstYearMatcher = line =~ /\d{4}/
    def correctedLine
    if (firstYearMatcher.find()) {
        def firstYear = firstYearMatcher.group(0)
        correctedLine = line.replaceFirst(/\d{4}(.*\d{4})*/, firstYear + '-' + year) + EOL
    }
    else {
        println('No copyright year in copyright line. Line not corrected: '+line)
        correctedLine = line + EOL
        //correctedLine = 'Copyright '+ year +' Ellucian Company L.P. and its affiliates.\n'
    }
    return correctedLine
}

def getNewCopyrightText(fileExt, year, EOL){
    def text = '*******************************************************************************'+EOL+'  Copyright ' +
            year + ' Ellucian Company L.P. and its affiliates.'+EOL+'*******************************************************************************'
    def delim
    switch(fileExt) {
        case ["groovy", "java", "js", "ts", "css"]:
            delim = ['/*', '*/']
            break
        case ["html", "xml"]:
            delim = ['<!--', '-->']
            break
        case "gsp":
            delim = ['%{--', '--}%']
            break
        case "sql":
            delim = ['--', '']
            text = text.replaceAll(EOL, EOL+"--")
            break
        default:
            throw new RuntimeException('Unrecognized file extension: '+ fileExt)
    }
    text = delim[0] + text + delim[1] + EOL

    return text;
}

@Category(RandomAccessFile)
class RandomAccessFileEach {
    /**
    * Iterates through the given RandomAccessFile line by line
    *
    * @param self    a RandomAccessFile
    * @param closure a closure
    * @throws IOException
    */
    public static void eachLine(RandomAccessFile self, Closure closure) throws IOException {
        try {
            while (true) {
                String line = self.readLine()

                if (line) {
                    if(!closure.call(line)) break
                } else if(self.getFilePointer() < self.length()) {
                    continue
                } else {
                    break
                }
            }
        } catch (IOException e) {
            try {
                self?.close()
            } catch (e2) {
                // ignore as we're already throwing
            }
            throw e
        }
    }
}
