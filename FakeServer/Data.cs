using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;
using System.Threading.Tasks;

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

    public class exam {
        public string name;
        public string date;
        public string time;
        public string examStartTime;
        public string examEndTime;
        public string room;
        public string semester;
        public string moed;
    }
    public class Exams {
        public exam[] exams;
    }

    public class Student {
        public string username;
        public string password;
        public string real;
        public string unique;
        public string ticket;
        public YearGrades yearGrades;
        public Exams exams;

    }

    public class Data {
        public static Student[] students = new Student[] {
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
                } },
                exams=new Exams {exams=new exam[] {
                    new exam {
                        name="foo",date="01/02/0003",time="13:00 עד 16:00",
                        examStartTime="01/02/0003T13:00",examEndTime="01/02/0003T16:00",
                        room="רבין           חדר 501,502",semester="what",moed="מועד ב"
                    },
                    new exam {
                        name="bar",date="5000BC",time="No time",
                        examStartTime="no start",examEndTime="no end",
                        room="bbq",semester="b**ch",moed="moed?"
                    },
                }}
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
                } },
                exams=new Exams {exams=new exam[] {
                    new exam {
                        name="???",date="32/13/3001",time="Future",
                        examStartTime="",examEndTime="",
                        room="",semester="",moed="מועד א"
                    },
                }}
            },
        };
    }
}
