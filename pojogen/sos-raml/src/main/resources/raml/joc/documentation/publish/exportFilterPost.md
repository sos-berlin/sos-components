<tr>
<td>``forSigning``</td>
<td>required, boolean</td>
<td>This property decides if the export is meant to be used to sign the exported objects.</td>
<td>  "forSigning" : false</td>
<td>false</td>
</tr>
<tr>
<td>``controllerId``</td>
<td>optional, string</td>
<td>This property sets the controller the export is intended for.<ul><li>Required if ``forSigning`` is true.</li><li>Will be ignored if ``forSigning`` is false.</li></ul></td>
<td>  "controllerId" : "testsuite"</td>
<td></td>
</tr>
<tr>
<td>``deployables``</td>
<td>optional, array</td>
<td>Filter collection of deployable configurations specified by their path and objectType. Deployable objects can be in draft state or already deployed objects.</br>Any of ``deployables`` or ``releasables`` is required.</td>
<td>"deployables" : {  
<div style="padding-left:10px;">"draftConfigurations" : [ {</div>
<div style="padding-left:20px;">"draftConfiguration" : {</div>
<div style="padding-left:30px;">"path" : "/myWorkflows/ifElseWorkflow/workflow_10",</div>
<div style="padding-left:30px;">"objectType" : "WORKFLOW"</div>
<div style="padding-left:20px;">}</div>
<div style="padding-left:10px;">}, {</div>
<div style="padding-left:20px;">"draftConfiguration" : {</div>
<div style="padding-left:30px;">"path" : "/myWorkflows/ifElseWorkflow/workflow_16",</div>
<div style="padding-left:30px;">"objectType" : "WORKFLOW"</div>
<div style="padding-left:20px;">}</div>
<div style="padding-left:10px;">}],</div>
<div style="padding-left:10px;">"deployConfigurations" : [ {</div>
<div style="padding-left:20px;">"deployConfiguration" : {</div>
<div style="padding-left:30px;">"path" : "/myWorkflows/ifElseWorkflow/workflow_12",</div>
<div style="padding-left:30px;">"objectType" : "WORKFLOW",</div>
<div style="padding-left:30px;">"commitId" : "4273b6c6-c354-4fcd-afdb-2758088abe4a"</div>
<div style="padding-left:20px;">}</div>
<div style="padding-left:10px;">}]</div>
}</td>
<td></td>
</tr>
<tr>
<td>``releasables``</td>
<td>optional, array</td>
<td>Filter collection of releasable configurations specified by their path and objectType. Releasable objects can be in draft state or already released objects.</br>Any of ``deployables`` or ``releasables`` is required.</td>
<td>"releasables" : {  
<div style="padding-left:10px;">"draftConfigurations" : [ {</div>
<div style="padding-left:20px;">"draftConfiguration" : {</div>
<div style="padding-left:30px;">"path" : "mySchedules/newSchedules/mySchedule",</div>
<div style="padding-left:30px;">"objectType" : "SCHEDULE"</div>
<div style="padding-left:20px;">}</div>
<div style="padding-left:10px;">}],</div>
<div style="padding-left:10px;">"deployConfigurations" : [ {</div>
<div style="padding-left:20px;">"deployConfiguration" : {</div>
<div style="padding-left:30px;">"path" : "/myCalendars/newCalendars/myCalendar",</div>
<div style="padding-left:30px;">"objectType" : "WORKINGDAYSCALENDAR"</div>
<div style="padding-left:20px;">}</div>
<div style="padding-left:10px;">}]</div>
}</td>
<td></td>
</tr>
<tr>
<td>``comment``</td>
<td>optional, string</td>
<td>for auditLog</td>
<td></td>
<td></td>
</tr>
<tr>
<td>``timeSpent``</td>
<td>optional, string</td>
<td>for auditLog</td>
<td></td>
<td></td>
</tr>
<tr>
<td>``ticketLink``</td>
<td>optional, string</td>
<td>for auditLog</td>
<td></td>
<td></td>
</tr>
