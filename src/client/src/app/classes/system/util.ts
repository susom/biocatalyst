import { Logger } from '../logger';

export class Util {
  public static CSVToText(Data: any[], csvSeparator = ','): string {
    let csv = '';
    // headers
    for (let header in Data[0]) {
      csv += header;
      csv += csvSeparator;
    }

    // body
    for (let obj of Data) {
      csv += '\n';
      for (let property in obj) {
        if (property != 'links') {
          csv += obj[property];
          csv += csvSeparator;
        }
      }
    }
    return csv;
  }

  public static DownloadCSV(Data: string | any[], Filename = 'Biosearch CSV') {
    let csv: string = Array.isArray(Data) ? this.CSVToText(Data) : Data;

    let blob = new Blob([csv], {
      type: 'text/csv;charset=utf-8;'
    });
    if (window.navigator.msSaveOrOpenBlob) {
      navigator.msSaveOrOpenBlob(blob, Filename + '.csv');
    } else {
      let link = document.createElement('a');
      link.style.display = 'none';
      document.body.appendChild(link);
      if (link.download !== undefined) {
        link.setAttribute('href', URL.createObjectURL(blob));
        link.setAttribute('download', Filename + '.csv');
        document.body.appendChild(link);
        link.click();
      } else {
        csv = 'data:text/csv;charset=utf-8,' + csv;
        window.open(encodeURI(csv));
      }
      document.body.removeChild(link);
    }
  }

  public static PrintCSV(Data: any[], SelectHeaders?: string[]) {

    Logger.console('PrintCSV', Data, SelectHeaders);

    let mywindow = window.open('', 'PRINT', 'height=800,width=1200');
    mywindow.document.write('<html><head><title>' + document.title + '</title>');
    mywindow.document.write('</head><body >');
    mywindow.document.write('<h1>' + document.title + '</h1>');
    mywindow.document.write('<style>table { font-family: arial, sans-serif; border-collapse: collapse; width: 100%;}td, th { border: 1px solid #dddddd; text-align: left; padding: 8px;}tr:nth-child(even) { background-color: #dddddd;}</style>');
    mywindow.document.write('</head><body >');
    mywindow.document.write('<table>');

    if (!SelectHeaders) {
      SelectHeaders = [];
      for (let prop in Data[0]) {
        SelectHeaders.push(prop);
      }
    }

    mywindow.document.write('<tr>');
    for (let header of SelectHeaders) {
      mywindow.document.write('<th>' + header + '</th>');
    }
    mywindow.document.write('</tr>');

    for (let item of Data) {
      mywindow.document.write('<tr>');
      for (let header of SelectHeaders) {
        mywindow.document.write('<th>' + ((item[header] != null) ? item[header] : '') + '</th>');
      }
      mywindow.document.write('<tr>');
    }

    mywindow.document.write('</table>');

    mywindow.document.write('</body></html>');

    mywindow.document.close(); // necessary for IE >= 10
    mywindow.focus(); // necessary for IE >= 10*/

    mywindow.print();
    mywindow.close();

    return true;

  }

  public static strcmp(A: string, B: string) {
    let headerA: string = A.toUpperCase(); // ignore upper and lowercase
    let headerB: string = B.toUpperCase(); // ignore upper and lowercase
    if (headerA < headerB) {
      return -1;
    }
    if (headerA > headerB) {
      return 1;
    }

    // names must be equal
    return 0;
  }
}
