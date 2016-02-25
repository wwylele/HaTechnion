﻿using System;
using System.Linq;
using System.Text;
using System.Net;
using System.Web;
using System.Xml;
using System.Xml.Linq;
using System.Xml.Serialization;
using System.IO;

namespace FakeServer {

    public class grade {
        public string name;
        public string final_grade;
        public string credits;
    }
    public class year_grade {
        public string year;
        public string year_system;
        public string average_grade;
        public grade[] grades;
    }

    public class YearGrades {
        public year_grade[] year_grades;
    }

    public class Student {
        public string username;
        public string password;
        public string real;
        public string unique;
        public string ticket;
        public YearGrades yearGrades;

    }

    class Program {

        static Student[] students = new Student[] {
            new Student {
                username="wwylele",password="pass",real="WWYLELE",
                yearGrades=new YearGrades { year_grades=new year_grade[] {
                    new year_grade {
                        year="first",year_system="1",average_grade="50",
                        grades=new grade[] {
                            new grade {
                                name="foo",final_grade="90",credits="3"
                            },
                            new grade {
                                name="bar",final_grade="10",credits="2"
                            }
                        }
                    },
                    new year_grade {
                        year="second",year_system="2",average_grade="100",
                        grades=new grade[] {
                            new grade {
                                name="f***",final_grade="100",credits="0"
                            }
                        }
                    },
                } }
            },
            new Student {
                username="test",password="123456",real="TEST!",
                yearGrades=new YearGrades { year_grades=new year_grade[] {
                    new year_grade {
                        year="test year",year_system="-1",average_grade="0",
                        grades=new grade[] {
                            new grade {
                                name="what",final_grade="0",credits="10"
                            }
                        }
                    },
                } }
            },
        };

        static void Main(string[] args) {
            Random random = new Random();

            HttpListener listener = new HttpListener();
            listener.Prefixes.Add("http://*:8888/");
            Console.WriteLine("HaTechnion fake server");
            listener.Start();
            Console.WriteLine("start listening!");


            while(true) {
                HttpListenerContext context = listener.GetContext();
                HttpListenerRequest request = context.Request;
                Console.WriteLine(request.HttpMethod + ":" + request.Url.LocalPath);

                HttpListenerResponse response = context.Response;
                string responseString = "<HTML><BODY> What do you want?</BODY></HTML>";
                if(request.HttpMethod == "POST" && request.Url.LocalPath == "/fake") {
                    try {
                        string requestDetail = "";
                        using(var reader = new StreamReader(request.InputStream)) {
                            requestDetail = reader.ReadToEnd();
                        }
                        const string requestHeader = "appRequest=";
                        if(requestDetail.IndexOf(requestHeader) != 0)
                            throw new Exception("invalid request");
                        requestDetail = HttpUtility.UrlDecode(requestDetail.Substring(requestHeader.Length));

                        XmlDocument xmlIn = new XmlDocument();
                        xmlIn.LoadXml(requestDetail);
                        XmlNode nodeApp = xmlIn.SelectSingleNode("app");
                        string req = nodeApp.SelectSingleNode("req").InnerText;
                        string club = nodeApp.SelectSingleNode("club").InnerText;
                        Console.WriteLine("req=" + req);
                        if(club != "81") throw new Exception("club!=81");

                        string retCode = "0";
                        string retText = "";
                        XElement dataX = new XElement("data");
                        switch(req) {
                        case "203": {
                                string username = nodeApp.SelectSingleNode("login_name").InnerText;
                                string password = nodeApp.SelectSingleNode("pass").InnerText;
                                string unique = nodeApp.SelectSingleNode("unique_id").InnerText;
                                var cursor = from s in students where s.username == username select s;
                                if(cursor.Any()) {
                                    Student student = cursor.First();
                                    if(student.password == password) {
                                        student.unique = unique;
                                        student.ticket = random.Next().ToString();
                                        dataX = new XElement("data",
                                            new XElement("id", student.ticket),
                                            new XElement("name", student.real),
                                            new XElement("user_id", username));
                                    } else {
                                        retCode = "-1";
                                        retText = "wrong password";
                                    }
                                } else {
                                    retCode = "-1";
                                    retText = "bad login name";
                                }
                                break;
                            }
                        case "185": {
                                string ticket = nodeApp.SelectSingleNode("id").InnerText;
                                string unique = nodeApp.SelectSingleNode("unique_id").InnerText;
                                var cursor = from s in students where
                                             s.unique == unique && s.ticket == ticket select s;
                                if(cursor.Any()) {
                                    Student student = cursor.First();
                                    XmlSerializer ser = new XmlSerializer(typeof(YearGrades));
                                    using(var stream = new MemoryStream()) {
                                        ser.Serialize(stream, student.yearGrades);
                                        stream.Position = 0;
                                        using(XmlReader reader = XmlReader.Create(stream)) {
                                            dataX = XElement.Load(reader);
                                            dataX.Name = "data";
                                        }
                                    }
                                } else {
                                    retCode = "-1";
                                    retText = "bad ticket";
                                }
                                break;
                            }
                        default:
                            throw new Exception("unknown req");
                        }

                        responseString = new XElement("app",
                            new XElement("result",
                                new XElement("ret_code", retCode),
                                new XElement("text", retText)),
                            dataX).ToString();

                        Console.WriteLine(responseString);

                    } catch(Exception e) {
                        Console.WriteLine(e);
                    }
                }
                byte[] buffer = Encoding.UTF8.GetBytes(responseString);
                response.ContentLength64 = buffer.Length;
                var output = response.OutputStream;
                output.Write(buffer, 0, buffer.Length);
                output.Close();

            }



        }
    }
}