<tr>
<td>``account``</td>
<td>optional, string</td>
<td>Profile (account) the deployment was done with.</td>
<td>
  <div>{</div>
  <div style="padding-left:10px;">"account": "root"</div>
  <div>}</div>
</td>
<td></td>
</tr>
<tr>
<td>``path``</td>
<td>optional, string</td>
<td>Path of a single deployed object.</td>
<td>
  <div>{</div>
  <div style="padding-left:10px;">"path": "/myWorkflows/ifElseWorkflows/test_workflow_01"</div>
  <div>}</div>
</td>
<td></td>
</tr>
<tr>
<td>``folder``</td>
<td>optional, string</td>
<td>Path of a folder of multiple objects.</td>
<td>
  <div>{</div>
  <div style="padding-left:10px;">"folder": "/myWorkflows/ifElseWorkflows"</div>
  <div>}</div>
</td>
<td></td>
</tr>
<tr>
<td>``controllerId``</td>
<td>optional, string</td>
<td>Name of the controller the deployment was processed to.</td>
<td>
  <div>{</div>
  <div style="padding-left:10px;">"controllerId": "testsuite"</div>
  <div>}</div>
</td>
<td></td>
</tr>
<tr>
<td>``commitId``</td>
<td>optional, string</td>
<td>Commit ID of the deployment.</td>
<td>
  <div>{</div>
  <div style="padding-left:10px;">"commitId": "4cbb095d-b998-4091-92f2-4fb8efb58805"</div>
  <div>}</div>
</td>
<td></td>
</tr>
<tr>
<td>``version``</td>
<td>optional, string</td>
<td>User defined version of a deployed object.</td>
<td>
  <div>{</div>
  <div style="padding-left:10px;">"version": "0.0.1"</div>
  <div>}</div>
</td>
<td></td>
</tr>
<tr>
<td>``deployType``</td>
<td>optional, string</td>
<td>Type of the deployed object. Available types are WORKFLOW, JOBCLASS, AGENTREF, LOCK, JUNCTION.</td>
<td>
  <div>{</div>
  <div style="padding-left:10px;">"deployType": "WORKFLOW"</div>
  <div>}</div>
</td>
<td></td>
</tr>
<tr>
<td>``operation``</td>
<td>optional, string</td>
<td>The operation of the deployment. Possible values are UPDATE, DELETE.</td>
<td>
  <div>{</div>
  <div style="padding-left:10px;">"operation": "UPDATE"</div>
  <div>}</div>
</td>
<td></td>
</tr>
<tr>
<td>``state``</td>
<td>optional, string</td>
<td>State of the deployment. Possible values are DEPLOYED, NOT_DEPLOYED</td>
<td>
  <div>{</div>
  <div style="padding-left:10px;">"state": "DEPLOYED"</div>
  <div>}</div>
</td>
<td></td>
</tr>
<tr>
<td>``deploymentDate``</td>
<td>optional, Date</td>
<td>Specific date when the deployment was done.</td>
<td>
  <div>{</div>
  <div style="padding-left:10px;">"deploymentDate": "2020-11-06T11:00:00Z"</div>
  <div>}</div>
</td>
<td></td>
</tr>
<tr>
<td>``deleteDate``</td>
<td>optional, Date</td>
<td>Specific date when an existing deployment was deleted from a controller.</td>
<td>
  <div>{</div>
  <div style="padding-left:10px;">"deleteDate": "2020-11-06T11:00:00Z"</div>
  <div>}</div>
</td>
<td></td>
</tr>
<tr><td>``from``</td><td>optional, string</td><td>The value has multiple formats
<ul>
<li>format for a date in ISO 8601 format where the <i>time offset</i> and milliseconds are optional, e.g.
  <ul>
    <li>YYYY-MM-DDThh:mm:ss[.s][Z (Z means +00)]</li>
    <li>YYYY-MM-DDThh:mm:ss[.s][+01:00]</li>
    <li>YYYY-MM-DDThh:mm:ss[.s][+0100]</li>
    <li>YYYY-MM-DDThh:mm:ss[.s][+01]</li>
  </ul>
</li>
<li>a format for a time period in relative to the current time, e.g. 6h, 12h, 1d, 1w can specify in addition with a time offset 0 or digits followed by a letter are expected where the letter has to be:
  <ul>
    <li>s (seconds)</li>
    <li>m (minutes)</li>
    <li>h (hours)</li>
    <li>d (days)</li>
    <li>w (weeks)</li>
    <li>M (months)</li>
    <li>y (years)</li>
  </ul>
</li>
<li>a time offset is optional (e.g. 2d+02:00)
  <ul>
    <li>it can be also specify with the parameter ``timeZone``</li>
    <li>if ``timeZone`` undefined then UTC is used</li>
  </ul>
</li>
<li>the value 0 means the current time</li>
<li>start date of a range of dates</li>
</ul>
</td><td>1d</td><td></td>
</tr>
<tr>
<td>``to``</td>
<td>optional, string</td>
<td>The value has multiple formats like the ``from`` parameter
  <ul>
    <li>end date of a range of dates</li>
  </ul>
</td>
<td>0</td>
<td></td>
</tr>