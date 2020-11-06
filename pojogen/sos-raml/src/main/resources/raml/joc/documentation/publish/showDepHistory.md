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
<td>``type``</td>
<td>optional, string</td>
<td>Type of the deployed object. Available types are WORKFLOW, JOBCLASS, AGENTREF, LOCK, JUNCTION.</td>
<td>
  <div>{</div>
  <div style="padding-left:10px;">"type": "WORKFLOW"</div>
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
<tr>
<td>``from``</td>
<td>optional, Date</td>
<td>Start date for a range of dates.</td>
<td>
  <div>{</div>
  <div style="padding-left:10px;">"from": "2020-06-01"</div>
  <div>}</div>
</td>
<td></td>
</tr>
<tr>
<td>``to``</td>
<td>optional, Date</td>
<td>End date for a range of dates.</td>
<td>
  <div>{</div>
  <div style="padding-left:10px;">"to": "2020-2-31"</div>
  <div>}</div>
</td>
<td></td>
</tr>
