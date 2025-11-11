// 수정한 코드 만 적었음

/**
 * 강의 정보를 YAML 파일로 관리하는 저장소 클래스
 * 싱글톤 패턴을 사용하며, 파일이 없을 경우 resources에서 복사하여 생성한다.
 * @author oixikite
 * @modifier oxxultus
 * @since 2025.05.16
 */
public class LectureRepository {

    /**
     * 동일 강의실/요일/학년/학기에서 시간 겹침 존재 여부
     * newLecture와 id가 같은 기존 항목은 제외(수정 시 자기 자신 제외)
     * @modifier 영진
     * @time 2025.11.11 11:00
     * 기존 방식의 문제점...
     * 1. 기존 코드의 If 조건문에서 l.getYear()랑 newLecture.getYear() 필드를 비교할 때 == 연산자를 사용한게 문제인듯
     * 2. Lecture 코드에서 Int가 아니라 Integer 클래스로 선언하고 있어서 객체 단위로 비교연산자를 사용하면 메모리 주소를 비교해서 항상 다르게 나와서 검증로직이 실행이 안되었음
     * 3. 그래서 모든 경우에서 l.getYear()와 newLecture.getYear()를 문자열로 변환해서 문자열 비교를 진행함
     * 4. 위 방식을 사용 안하려면 Lecture 클래스의 변수 선언부분을 전부 수정해야됨
     *
     * DayOfWeek lecDay = convertToDayOfWeekEnum(lec.getDay()); 로 월~일 을 변환하는 로직이 있지만
     * enum 같은 경우도 타입 불일치 발생할 수 있어서 String 으로 문자열만 가져와서 비교하는 방식으로 수정하면 좋을 듯
     */
    public boolean hasTimeConflict(Lecture newLecture) {

        // 문제점 발생 원인을 찾기 위한 전달 받은 값 출력
        System.out.println("\n--- 강의실 중복 검증 시작 ---");
        System.out.println("새 강의 정보: " + newLecture.getTitle() + " (" + newLecture.getId() + ")");
        System.out.println("  > " + newLecture.getYear() + "년 " + newLecture.getSemester() + ", " +
                newLecture.getBuilding() + " " + newLecture.getLectureroom() + ", " +
                newLecture.getDay() + " (" + newLecture.getStartTime() + "~" + newLecture.getEndTime() + ")");
        System.out.println("-------------------------");

        // newLecture의 Year와 Floor를 String으로 변환 (타입 안정성 확보)
        // 기존 비교 연산을 사용하지 않고 문자열로 변환해서 비교를 수행 기존 값을 수정하거나 하지는 않고
        // 비교 검증에만 새로운 변수를 생성해서 비교함
        String newYearStr = String.valueOf(newLecture.getYear());
        String newFloorStr = newLecture.getFloor();

        for (Lecture l : lectureList) {

            // 자기 자신 제외 (수정 시)
            if (l.getId() != null && l.getId().equals(newLecture.getId())) {
                System.out.println("[PASS] ID가 같아 검사 대상에서 제외: " + l.getId());
                continue;
            }

            // l 객체의 Year와 Floor도 String으로 변환
            String lYearStr = String.valueOf(l.getYear());
            String lFloorStr = l.getFloor();

            // 2. 물리적/학기적 조건 비교 (모든 비교를 .equals()로 통일)
            boolean isSameContext = lYearStr.equals(newYearStr)
                    && l.getSemester().equals(newLecture.getSemester())
                    && l.getBuilding().equals(newLecture.getBuilding())
                    && lFloorStr.equals(newFloorStr)
                    && l.getLectureroom().equals(newLecture.getLectureroom())
                    && l.getDay().equals(newLecture.getDay());

            // 3. 조건 불일치 시 상세 로그 출력
            /*
            if (!isSameContext) {
                System.out.println("[FILTERED] 조건 불일치 강의: " + l.getTitle() + " (" + l.getId() + ")");
                System.out.println("  > 기존 정보: " + lYearStr + "/" + l.getSemester() +
                        ", 강의실: " + l.getBuilding() + " " + l.getLectureroom() +
                        ", 요일: " + l.getDay());

                System.out.println("    --- 불일치 세부 원인 ---");
                if (!lYearStr.equals(newYearStr)) System.out.println("    - 원인: 년도 불일치 (" + lYearStr + " != " + newYearStr + ")");
                if (!l.getSemester().equals(newLecture.getSemester())) System.out.println("    - 원인: 학기 불일치");
                if (!l.getBuilding().equals(newLecture.getBuilding())) System.out.println("    - 원인: 건물 불일치");
                if (!lFloorStr.equals(newFloorStr)) System.out.println("    - 원인: 층 불일치");
                if (!l.getLectureroom().equals(newLecture.getLectureroom())) System.out.println("    - 원인: 강의실 불일치");
                if (!l.getDay().equals(newLecture.getDay())) System.out.println("    - 원인: 요일 불일치");
                System.out.println("--------------------------");
                continue;
            }
            */

            // 4. 시간 겹침 검사
            if (timeOverlaps(l.getStartTime(), l.getEndTime(),
                    newLecture.getStartTime(), newLecture.getEndTime())) {

                // 찾은 부분 확인
                System.out.println("\n### [CONFLICT FOUND] 시간표 충돌 발견! ###");
                System.out.println("    > 기존 강의: " + l.getTitle() + " (" + l.getId() + ")");
                System.out.println("    > 시간대: " + l.getStartTime() + " ~ " + l.getEndTime());
                System.out.println("#########################################\n");
                return true;
            }
        }

        System.out.println("--- 강의실 중복 검증 완료: 충돌 없음 ---");
        return false;
    }

    // 시각 "HH:mm" 문자열 비교용: startA < endB && startB < endA 면 겹침
    private static boolean timeOverlaps(String startA, String endA, String startB, String endB) {
        return startA.compareTo(endB) < 0 && startB.compareTo(endA) < 0;
    }

}
