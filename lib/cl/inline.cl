;; @main
(main)

;; @extend
(defclass Object () (()))

;; @Fault
(define-condition Fault (error)
  ((msg :initarg :msg :reader msg)))

;; @SoftwareFault;@Fault
(define-condition SoftwareFault (Fault)
  ())

;; @catch;@SoftwareFault
(defun libbun-catch (e)
  (cond ((eq (type-of e) 'Fault) e)
        ((or (eq (type-of e) 'division-by-zero) ; sbcl
             (eq (type-of e) 'system::simple-division-by-zero)) ; clisp
         (let ((e1 (make-instance 'SoftwareFault)))
           (setf (slot-value e1 'msg) "division-by-zero")
           e1))
        (t e)))

;; @mapget;@SoftwareFault
(defun libbun-mapget (m k &optional v)
  (multiple-value-bind (value found) (gethash k m)
    (if (not found)
        (or v (error 'SoftwareFault))
      value)))

;; @arrayget;@SoftwareFault
(defun libbun-arrayget (a i)
  (if (and (>= i 0) (< i (length a)))
      (nth i a)
    (error 'SoftwareFault)))

;; @arrayset;@SoftwareFault
(defun libbun-arrayset (a i v)
  (if (and (>= i 0) (< i (length a)))
      (setf (nth i a) v)
    (error 'SoftwareFault)))
