from requests import get
from bs4 import BeautifulSoup
from re import compile
from sqlite3 import OperationalError
from sqlalchemy import Column, Integer, String, Boolean, ForeignKey
from datetime import datetime
from settings import SESSION, BASE, UndergradCalendarBaseURL, MathDegreeRequirementsURL


baseURL = 'https://ugradcalendar.uwaterloo.ca'
programsURL = 'https://ugradcalendar.uwaterloo.ca/page/MATH-List-of-Academic-Programs-or-Plans'


class Requirement(BASE):
    __tablename__ = 'Requirement'
    requirementID = Column(Integer, primary_key=True, nullable=False)
    type = Column(String, nullable=False)
    year = Column(String, nullable=False)
    courses = Column(String, nullable=False)
    additionalRequirements = Column(String, nullable=True)
    link = Column(String, nullable=True)

    def __init__(self, type, year, courses, additionalRequirements, link):
        self.type = type
        self.year = year
        self.courses = courses
        self.additionalRequirements = additionalRequirements
        self.links = link


class Major(BASE):
    __tablename__ = 'Major'
    requirementID = Column(Integer, ForeignKey('Requirement.requirementID'), primary_key=True, nullable=False)
    majorName = Column(String, nullable=False)
    coopOnly = Column(Boolean, nullable=False)
    isDoubleDegree = Column(Boolean, nullable=False)

    def __init__(self, requirementID, majorName, isCoop, isDoubleDegree):
        self.requirementID = requirementID
        self.majorName = majorName
        self.isCoop = isCoop
        self.isDoubleDegree = isDoubleDegree


def getProgramRequirements(name, url, year):
    param = '?ActiveDate=9/1/' + str(year)
    html = get(UndergradCalendarBaseURL + url + param).text
    soup = BeautifulSoup(html, features='html.parser')
    requirements = soup.find('span', id='ctl00_contentMain_lblContent')
    aList = requirements.find_all('a', href=compile('page'))
    for a in aList[:3]:
        planName, url = a.get_text(), a['href']
        if not validatePlan(planName): continue
        parentTag = a.find_previous()
        if parentTag and parentTag.next_sibling and parentTag.next_sibling.name == 'ul':
            url = parentTag.next_sibling.find('a', text='Degree Requirements')['href']
        getRequirement(planName, url, year)


def validatePlan(planName):
    if planName == 'Admissions': return False
    if planName == 'Plan Requirements': return False
    if planName == 'Specializations': return False
    if planName == 'Overview': return False
    if planName == 'Degree Requirements': return False
    return True


def getRequirement(name, url, year):
    param = '?ActiveDate=9/1/' + str(year)
    html = get(UndergradCalendarBaseURL + url + param).text
    soup = BeautifulSoup(html, features='html.parser')
    contents = soup.find('span', id='ctl00_contentMain_lblContent')
    aList = [a.get_text() for a in contents.find_all('a')]
    res, courses = [], set()
    if 'Table 1' in aList and 'Table 2' in aList: res += getTable2Courses(year)
    choices = contents.find_next('ul').contents
    choices = list(filter(lambda c: c != '\n', choices))
    for choice in choices[:5]:
        r, c = parseChoice(choice)
        res += r
        courses = courses.union(c)
    # res = updateRequirement(res)
    # print(res)


def getTable2Courses(year):
    param = '?ActiveDate=9/1/' + str(year)
    html = get(MathDegreeRequirementsURL + param).text
    soup = BeautifulSoup(html, features='html.parser')
    choices = soup.find(string='Table 2 – Faculty Core Courses').find_next('ul').contents
    choices = list(filter(lambda c: c != '\n', choices))
    res, courses = [], set()
    for choice in choices:
        r, c = parseChoice(choice)
        res += r
        courses = courses.union(set(c))
    print(res)
    print(courses)
    return res, courses


def updateRequirement(requirement, courses, choice):
    r, _ = parseChoice(choice)
    for req in r:
        n, options = req[0], req[1]
        for option in options:
            if option in courses:
                print(option)

    # for i, req in enumerate(requirement):
    #     n, options = req[0], req[1]
    #     for j, r in enumerate(requirement):
    #         if j == i: continue
    #         count = 0
    #         for option in options:
    #             if option in r[1]: count += 1
    #         print(count)


def parseChoice(choice):
    logic = choice.contents[0].lower()
    options = [course.find('a').get_text() for course in choice.find_all('li')]
    n, res, courses = 0, [], []
    if logic == compile('all'):
        for option in options: 
            res.append((1, option))
            courses.append(option)
        return res
    elif 'one' in logic: n = 1
    elif logic == compile('two'): n = 2
    elif logic == compile('three'): n = 3
    elif logic == compile('four'): n = 4
    elif logic == compile('five'): n = 5
    elif logic == compile('six'): n = 6
    elif logic == compile('seven'): n = 7
    elif logic == compile('eight'): n = 8
    elif logic == compile('nine'): n = 9
    elif logic == compile('ten'): n = 10
    else: 
        print(f'Invalid number for:\n{logic}')
        return res
    res.append((n, options))
    for option in options: courses.append(option)
    return res, courses
    

if __name__ == '__main__':
    getTable2Courses(2023)
    # getProgramRequirements('Computer Science', '/group/MATH-Computer-Science-1', 2023)
    getRequirement('Bachelor of Computer Science', '/page/MATH-Bachelor-of-Computer-Science-1', 2023)
