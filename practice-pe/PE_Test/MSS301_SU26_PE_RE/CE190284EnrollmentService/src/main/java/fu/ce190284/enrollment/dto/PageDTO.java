package fu.ce190284.enrollment.dto;
import org.springframework.data.domain.Page;import java.util.List;
public record PageDTO<T>(int size,int page,int totalPages,long totalElements,boolean first,boolean last,List<T> content){public static <T> PageDTO<T> of(Page<T> p){return new PageDTO<>(p.getSize(),p.getNumber(),p.getTotalPages(),p.getTotalElements(),p.isFirst(),p.isLast(),p.getContent());}}
