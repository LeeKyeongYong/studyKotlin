package com.krstudy.kapi.standard.base

/**
 * URL의 쿼리 파라미터를 수정하는 유틸리티 클래스를 제공한다.
 * 이 클래스는 URL의 쿼리 파라미터를 추가하거나 삭제하는 기능을 포함한다.
 */
class Ut {
    companion object {

        /**
         * 주어진 URL의 쿼리 파라미터를 수정한다.
         * 기존의 동일한 파라미터가 존재하는 경우, 해당 파라미터를 삭제한 후 새 값을 추가한다.
         *
         * @param url 쿼리 파라미터를 수정할 URL
         * @param paramName 수정할 파라미터의 이름
         * @param paramValue 수정할 파라미터의 값
         * @return 수정된 URL
         */
        fun modifyQueryParam(url: String, paramName: String, paramValue: String): String {
            var updatedUrl = deleteQueryParam(url, paramName)
            updatedUrl = addQueryParam(updatedUrl, paramName, paramValue)
            return updatedUrl
        }

        /**
         * 주어진 URL에 쿼리 파라미터를 추가한다.
         * 기존의 쿼리 파라미터가 없는 경우, URL에 쿼리 문자열을 추가한다.
         *
         * @param url 쿼리 파라미터를 추가할 URL
         * @param paramName 추가할 파라미터의 이름
         * @param paramValue 추가할 파라미터의 값
         * @return 쿼리 파라미터가 추가된 URL
         */
        fun addQueryParam(url: String, paramName: String, paramValue: String): String {
            var updatedUrl = url

            if (!updatedUrl.contains("?")) {
                updatedUrl += "?"
            }

            if (!updatedUrl.endsWith("?") && !updatedUrl.endsWith("&")) {
                updatedUrl += "&"
            }

            updatedUrl += "$paramName=$paramValue"
            return updatedUrl
        }

        /**
         * 주어진 URL에서 특정 쿼리 파라미터를 삭제처리 한다.
         * 파라미터가 URL에 존재하지 않는 경우, 원래의 URL을 반환한다.
         *
         * @param url 쿼리 파라미터를 삭제할 URL
         * @param paramName 삭제할 파라미터의 이름
         * @return 쿼리 파라미터가 삭제된 URL
         */
        fun deleteQueryParam(url: String, paramName: String): String {
            val paramWithEqualSign = "$paramName="
            val startPoint = url.indexOf(paramWithEqualSign)
            if (startPoint == -1) return url

            val endPoint = url.indexOf("&", startPoint)
            return if (endPoint == -1) {
                url.substring(0, startPoint - 1).takeIf { it.isNotEmpty() } ?: url
            } else {
                url.substring(0, startPoint) + url.substring(endPoint + 1)
            }
        }
    }
    object str {
        fun isBlank(str: String?): Boolean {
            return str == null || str.trim().isEmpty()
        }

        fun hasLength(str: String?): Boolean {
            return !isBlank(str)
        }
    }

    object thread {
        fun sleep(millis: Long) = Thread.sleep(millis)
    }

}