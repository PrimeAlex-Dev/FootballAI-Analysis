import { useList } from '../shared/useList'
import { competitionSeasonApi } from './api'
import CompetitionSeasonForm from './CompetitionSeasonForm'
import CatalogSelect from '../shared/CatalogSelect'
import {
  Button,
  ListTable,
  PageHeading,
  Pagination,
  RowActions,
} from '../shared/ui'
import { useRecordDialogs } from '../shared/useRecordDialogs'
export default function CompetitionSeasonsPage() {
  const list = useList(competitionSeasonApi.list)
  const records = useRecordDialogs(competitionSeasonApi, list.refresh)
  return (
    <>
      <PageHeading
        title="Giải đấu theo mùa"
        description="Liên kết giải đấu với mùa giải đã có trong danh mục."
        createLabel="Tạo liên kết"
        onCreate={records.create}
      />
      {records.notice && (
        <p className="success-notice" role="status">
          {records.notice}
        </p>
      )}
      <section className="ui-card management-card">
        <div className="filter-bar">
          <CatalogSelect
            kind="competition"
            value={list.params.get('competitionId') || ''}
            onChange={(id) => list.change({ competitionId: id })}
          />
          <CatalogSelect
            kind="season"
            value={list.params.get('seasonId') || ''}
            onChange={(id) => list.change({ seasonId: id })}
          />
          <Button
            secondary
            onClick={() => list.change({ competitionId: '', seasonId: '' })}
          >
            Đặt lại
          </Button>
        </div>
        <ListTable
          headers={[
            'ID',
            'Giải đấu theo mùa',
            'ID giải đấu',
            'ID mùa giải',
            'Thao tác',
          ]}
          loading={list.loading}
          error={list.error}
          empty={!list.data?.content.length}
          retry={list.refresh}
        >
          {list.data?.content.map((item) => (
            <tr key={item.id}>
              <td>#{item.id}</td>
              <td>
                <strong>{item.displayName}</strong>
              </td>
              <td>#{item.competitionId}</td>
              <td>#{item.seasonId}</td>
              <td>
                <RowActions
                  view={() => records.select('view', item)}
                  remove={() => records.select('delete', item)}
                />
              </td>
            </tr>
          ))}
        </ListTable>
        <Pagination
          page={list.page}
          size={list.size}
          total={list.data?.page.totalElements || 0}
          pages={list.data?.page.totalPages || 0}
          disabled={list.loading || !!list.error}
          changeSize={(size) => list.change({ size })}
          go={list.go}
        />
      </section>
      {records.creating && (
        <CompetitionSeasonForm
          onClose={records.close}
          onSuccess={records.success}
        />
      )}
      {records.dialogs(
        (item) => item.displayName,
        (item) => (
          <>
            <dt>ID</dt>
            <dd>#{item.id}</dd>
            <dt>Tên hiển thị</dt>
            <dd>{item.displayName}</dd>
            <dt>ID giải đấu</dt>
            <dd>#{item.competitionId}</dd>
            <dt>ID mùa giải</dt>
            <dd>#{item.seasonId}</dd>
          </>
        ),
      )}
    </>
  )
}
