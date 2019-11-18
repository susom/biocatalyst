export interface IConfiguration {
  integrator_integrate_format: string;
  integrator_status_format: string;
  integrator_preprocessor_format: string;
  integrator_evaluate_format: string;

  biosearch_proxy_bookmarks: string;
  biosearch_proxy_collection_protocols: string;
  biosearch_proxy_configuration: string;
  biosearch_distinct_column_values: string;
  biosearch_proxy_download: string;
  biosearch_proxy_integration: string;
  biosearch_proxy_suggestion: string;
  biosearch_proxy_user: string;
  biosearch_proxy_search: string;

  biosearch_proxy_localstorage: string;
  biosearch_proxy_localstorage_create: string;
  biosearch_proxy_localstorage_preview: string;
  biosearch_proxy_localstorage_update: string;
  biosearch_proxy_localstorage_delete: string;

  biosearch_proxy_stride_irbs: string;
  biosearch_proxy_stride_metadata: string;

  biosearch_proxy_redcap_projects: string;
  biosearch_proxy_redcap_reports: string;
  biosearch_proxy_redcap_columns: string;

  systemAuthentication: boolean;
}

export class Configuration implements IConfiguration {
  integratorBase = '/integrator-0.1.0';
  integrator_integrate_format = `${this.integratorBase}/integrate`;
  integrator_status_format = `${this.integratorBase}/status`;
  integrator_preprocessor_format = `${this.integratorBase}/preprocessor`;
  integrator_evaluate_format = `${this.integratorBase}/evaluation`;

  biosearchProxyBase = '/biosearch-proxy/rest';
  biosearch_proxy_bookmarks = `${this.biosearchProxyBase}/bookmarks`;
  biosearch_proxy_collection_protocols = `${this.biosearchProxyBase}/collection-protocols`;
  biosearch_proxy_configuration = `${this.biosearchProxyBase}/configuration`;
  biosearch_proxy_download = `${this.biosearchProxyBase}/download`;
  biosearch_proxy_integration = `${this.biosearchProxyBase}/integration`;
  biosearch_proxy_suggestion = `${this.biosearchProxyBase}/suggestion`;
  biosearch_proxy_user = `${this.biosearchProxyBase}/user`;
  biosearch_proxy_search = `${this.biosearchProxyBase}/search`;
  biosearch_distinct_column_values = `${this.biosearchProxyBase}/distinct-column-values`;

  biosearchProxyStride = `${this.biosearchProxyBase}/stride`;
  biosearch_proxy_stride_irbs = `${this.biosearchProxyStride}/irbs`;
  biosearch_proxy_stride_metadata = `${this.biosearchProxyStride}/metadata`;

  biosearchProxyRedcap = `${this.biosearchProxyBase}/redcap`;
  biosearch_proxy_redcap_columns = `${this.biosearchProxyRedcap}/columns`;
  biosearch_proxy_redcap_projects = `${this.biosearchProxyRedcap}/projects`;
  biosearch_proxy_redcap_reports = `${this.biosearchProxyRedcap}/reports`;

  biosearch_proxy_localstorage = `${this.biosearchProxyBase}/localstorage`;
  biosearch_proxy_localstorage_create = `${this.biosearch_proxy_localstorage}/create`;
  biosearch_proxy_localstorage_delete = `${this.biosearch_proxy_localstorage}/delete`;
  biosearch_proxy_localstorage_preview = `${this.biosearch_proxy_localstorage}/preview`;
  biosearch_proxy_localstorage_update = `${this.biosearch_proxy_localstorage}/update`;

  systemAuthentication = false;
}




